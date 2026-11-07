import { fakeAsync, flushMicrotasks, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, NavigationEnd, Router } from '@angular/router';
import { provideIonicAngular } from '@ionic/angular/standalone';
import { of, Subject, throwError } from 'rxjs';
import { NotificationService } from '../../../../core/feedback/notification.service';
import { AppError } from '../../../../core/http/app-error';
import { ProdutoApiService } from '../../../product-pricing/data-access/produto-api.service';
import type { EntradaEstoqueResponse } from '../../../product-pricing/models/entrada-estoque';
import type { ProdutoResponse } from '../../../product-pricing/models/produto-response';
import { StockEntrySuccessStore } from '../../state/stock-entry-success.store';
import { REFRESH_NOTICE, StockEntryPage } from './stock-entry.page';

describe('StockEntryPage', () => {
  const product: ProdutoResponse = {
    id: 15,
    codigoBarras: '00123',
    descricao: 'Produto',
    unidade: 'UN',
    precoVenda: 9.5,
    precoCusto: 6.9,
    codigoNcm: '12345678',
    estoqueAtual: 18.5
  };
  const updatedProduct: ProdutoResponse = { ...product, estoqueAtual: 31, precoCusto: 7.35 };
  const result: EntradaEstoqueResponse = {
    produtoId: 15, estoqueAnterior: 18.5, estoqueAtual: 31, precoCustoAnterior: 6.9, precoCustoAtual: 7.35
  };
  const api = jasmine.createSpyObj<ProdutoApiService>('api', ['buscarPorCodigo', 'registrarEntradaEstoque']);
  const router = jasmine.createSpyObj<Router>('router', ['navigateByUrl']);
  const notifications = jasmine.createSpyObj<NotificationService>('notifications', ['show']);
  let store: StockEntrySuccessStore;

  const createPage = (): StockEntryPage => {
    const page = TestBed.createComponent(StockEntryPage).componentInstance;
    page.ngOnInit();
    return page;
  };

  const fill = (page: StockEntryPage, quantity: string, cost: string, expiry = ''): void => {
    page.form.controls.quantidade.setValue(quantity);
    page.formatCost(cost);
    page.form.controls.dataVencimento.setValue(expiry);
  };

  beforeEach(() => {
    sessionStorage.clear();
    Object.defineProperty(router, 'events', {
      value: of(new NavigationEnd(1, '/entrada-estoque/00123', '/entrada-estoque/00123')),
      configurable: true
    });
    api.buscarPorCodigo.and.returnValue(of(product));
    api.registrarEntradaEstoque.and.returnValue(of(result));
    router.navigateByUrl.and.resolveTo(true);
    TestBed.configureTestingModule({
      imports: [StockEntryPage],
      providers: [provideIonicAngular(),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ codigo: '00123' }) } } },
        { provide: ProdutoApiService, useValue: api },
        { provide: Router, useValue: router },
        { provide: NotificationService, useValue: notifications }]
    });
    store = TestBed.inject(StockEntrySuccessStore);
  });

  afterEach(() => {
    api.buscarPorCodigo.calls.reset();
    api.registrarEntradaEstoque.calls.reset();
    router.navigateByUrl.calls.reset();
    notifications.show.calls.reset();
    sessionStorage.clear();
  });

  it('consulta o produto pelo código da rota e exibe os dados da entrada', () => {
    const fixture = TestBed.createComponent(StockEntryPage);
    fixture.detectChanges();

    expect(api.buscarPorCodigo).toHaveBeenCalledOnceWith('00123');
    expect(fixture.componentInstance.loading()).toBeFalse();
    const text = fixture.nativeElement.textContent as string;
    for (const expected of ['00123', 'Produto', 'UN', 'Preço unitário', 'Preço de custo', 'Estoque atual', '18.5']) {
      expect(text).toContain(expected);
    }
    expect(text).not.toContain('NCM');
  });

  it('exibe traço para valores nulos', () => {
    api.buscarPorCodigo.and.returnValue(of({
      ...product, unidade: null as unknown as string, precoVenda: null as unknown as number,
      precoCusto: null, estoqueAtual: null
    }));
    const fixture = TestBed.createComponent(StockEntryPage);
    fixture.detectChanges();

    const values = Array.from(fixture.nativeElement.querySelectorAll('dd')) as HTMLElement[];
    const dash = values.filter((value) => value.textContent?.trim() === '—');
    expect(dash.length).toBe(4);
  });

  it('mostra erro para código de barras inválido sem consultar a API', () => {
    TestBed.inject(ActivatedRoute).snapshot.paramMap.get = () => 'abc';
    const page = createPage();
    expect(api.buscarPorCodigo).not.toHaveBeenCalled();
    expect(page.errorMessage()).toBe('O código de barras informado é inválido.');
    expect(page.loading()).toBeFalse();
  });

  it('registra a entrada pelo ID, sem dataVencimento quando não informada', fakeAsync(() => {
    const page = createPage();
    fill(page, '12,5', '735');
    page.save();
    flushMicrotasks();

    expect(api.registrarEntradaEstoque).toHaveBeenCalledOnceWith(15, { quantidade: 12.5, precoCusto: 7.35 });
    const body = api.registrarEntradaEstoque.calls.mostRecent().args[1];
    expect('dataVencimento' in body).toBeFalse();
    expect(notifications.show).toHaveBeenCalledWith(
      'Entrada no estoque registrada com sucesso.', { color: 'success', duration: 1800 });
    expect(router.navigateByUrl).toHaveBeenCalledWith('/entrada-estoque-sucesso');
  }));

  it('envia a dataVencimento quando informada', () => {
    const page = createPage();
    fill(page, '1', '100', '2027-03-15');
    page.save();

    expect(api.registrarEntradaEstoque).toHaveBeenCalledOnceWith(
      15, { quantidade: 1, precoCusto: 1, dataVencimento: '2027-03-15' });
  });

  it('guarda no store os valores devolvidos pela API', fakeAsync(() => {
    const page = createPage();
    fill(page, '12,5', '735');
    page.save();
    flushMicrotasks();

    expect(store.data()).toEqual({
      description: 'Produto', barcode: '00123', unit: 'UN',
      previousStock: 18.5, currentStock: 31, previousCost: 6.9, currentCost: 7.35
    });
  }));

  it('não envia formulário inválido', () => {
    const page = createPage();
    for (const [quantity, cost, expiry] of [
      ['', '100', ''], ['0', '100', ''], ['1,2345', '100', ''], ['abc', '100', ''],
      ['1', '', ''], ['1', '000', ''], ['1', '100', '2027-02-30'], ['1', '100', '15/03/2027']
    ]) {
      fill(page, quantity, cost, expiry);
      page.save();
    }
    expect(api.registrarEntradaEstoque).not.toHaveBeenCalled();
  });

  it('bloqueia envio duplo enquanto a primeira requisição está ativa', () => {
    const pending = new Subject<EntradaEstoqueResponse>();
    api.registrarEntradaEstoque.and.returnValue(pending);
    const page = createPage();
    fill(page, '1', '100');
    page.save();
    page.save();

    expect(page.saving()).toBeTrue();
    expect(api.registrarEntradaEstoque).toHaveBeenCalledTimes(1);
    pending.error(new AppError(400, 'Confira os dados informados e tente novamente.'));
    expect(page.saving()).toBeFalse();
  });

  it('em erro 400 ou 404 exibe a mensagem sem recarregar o produto nem limpar o formulário', () => {
    for (const status of [400, 404]) {
      api.buscarPorCodigo.calls.reset();
      api.registrarEntradaEstoque.and.returnValue(throwError(() => new AppError(status, 'Falha de validação.')));
      const page = createPage();
      fill(page, '5', '100');
      page.save();

      expect(page.errorMessage()).toBe('Falha de validação.');
      expect(api.buscarPorCodigo).toHaveBeenCalledTimes(1);
      expect(page.awaitingRefresh()).toBeFalse();
      expect(page.form.controls.quantidade.value).toBe('5');
    }
  });

  it('em erro ambíguo recarrega o produto, limpa quantidade e vencimento e não reenvia sozinho', () => {
    for (const status of [0, 500, 503]) {
      api.buscarPorCodigo.calls.reset();
      api.registrarEntradaEstoque.calls.reset();
      api.buscarPorCodigo.and.returnValues(of(product), of(updatedProduct));
      api.registrarEntradaEstoque.and.returnValue(throwError(() => new AppError(status, 'Sem conexão.')));
      const page = createPage();
      fill(page, '5', '100', '2027-03-15');
      page.save();

      expect(api.buscarPorCodigo).toHaveBeenCalledTimes(2);
      expect(api.registrarEntradaEstoque).toHaveBeenCalledTimes(1);
      expect(page.product()?.estoqueAtual).toBe(31);
      expect(page.form.controls.quantidade.value).toBe('');
      expect(page.form.controls.dataVencimento.value).toBe('');
      expect(page.refreshNotice()).toBe(REFRESH_NOTICE);
      expect(page.errorMessage()).toBe('Sem conexão.');
      expect(page.awaitingRefresh()).toBeFalse();
    }
  });

  it('exige nova quantidade depois da recarga e permite novo envio apenas após confirmação', () => {
    api.buscarPorCodigo.and.returnValues(of(product), of(updatedProduct));
    api.registrarEntradaEstoque.and.returnValues(throwError(() => new AppError(0, 'Sem conexão.')), of(result));
    const page = createPage();
    fill(page, '5', '100');
    page.save();

    page.save();
    expect(api.registrarEntradaEstoque).toHaveBeenCalledTimes(1);

    page.form.controls.quantidade.setValue('5');
    page.save();
    expect(api.registrarEntradaEstoque).toHaveBeenCalledTimes(2);
  });

  it('mantém o envio bloqueado enquanto a recarga falhar e libera ao conseguir atualizar', () => {
    api.buscarPorCodigo.and.returnValues(
      of(product), throwError(() => new AppError(0, 'Sem conexão.')), of(updatedProduct));
    api.registrarEntradaEstoque.and.returnValue(throwError(() => new AppError(503, 'Banco indisponível.')));
    const page = createPage();
    fill(page, '5', '100');
    page.save();

    expect(page.awaitingRefresh()).toBeTrue();
    expect(page.errorMessage()).toContain('Não foi possível conferir o estoque atual.');
    page.form.controls.quantidade.setValue('5');
    page.save();
    expect(api.registrarEntradaEstoque).toHaveBeenCalledTimes(1);

    page.refresh();
    expect(page.awaitingRefresh()).toBeFalse();
    expect(page.product()?.estoqueAtual).toBe(31);
  });

  it('calcula a prévia do estoque tratando estoque nulo como zero', () => {
    const page = createPage();
    expect(page.projectedStock()).toBeNull();
    page.form.controls.quantidade.setValue('12,5');
    expect(page.projectedStock()).toBe(31);
    page.form.controls.quantidade.setValue('0');
    expect(page.projectedStock()).toBeNull();

    api.buscarPorCodigo.and.returnValue(of({ ...product, estoqueAtual: null }));
    const withoutStock = createPage();
    withoutStock.form.controls.quantidade.setValue('2,5');
    expect(withoutStock.projectedStock()).toBe(2.5);
  });

  it('avisa sem bloquear quando o custo é maior que o preço unitário', () => {
    const page = createPage();
    page.formatCost('950');
    expect(page.costAboveSalePrice()).toBeFalse();
    page.formatCost('951');
    expect(page.costAboveSalePrice()).toBeTrue();
    page.form.controls.quantidade.setValue('1');
    expect(page.form.valid).toBeTrue();

    api.buscarPorCodigo.and.returnValue(of({ ...product, precoVenda: null as unknown as number }));
    const withoutPrice = createPage();
    withoutPrice.formatCost('99999');
    expect(withoutPrice.costAboveSalePrice()).toBeFalse();
  });

  it('formata os dígitos do custo com duas casas decimais fixas', () => {
    const page = createPage();
    page.formatCost('1');
    expect(page.form.controls.precoCusto.value).toBe('0,01');
    page.formatCost('R$ 12,34');
    expect(page.form.controls.precoCusto.value).toBe('12,34');
    page.formatCost('');
    expect(page.form.controls.precoCusto.value).toBe('');
  });

  it('volta ao scanner de entrada ao cancelar', () => {
    createPage().cancel();
    expect(router.navigateByUrl).toHaveBeenCalledWith('/scanner/entrada');
  });

  it('mostra o erro quando o produto não é encontrado na carga inicial', () => {
    api.buscarPorCodigo.and.returnValue(throwError(() => new AppError(404, 'Produto não encontrado.')));
    const page = createPage();
    expect(page.product()).toBeNull();
    expect(page.errorMessage()).toBe('Produto não encontrado.');
    expect(page.loading()).toBeFalse();
  });
});
