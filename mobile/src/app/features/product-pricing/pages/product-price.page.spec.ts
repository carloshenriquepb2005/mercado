import { fakeAsync, flushMicrotasks, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, NavigationEnd, Router } from '@angular/router';
import { provideIonicAngular, ToastController } from '@ionic/angular/standalone';
import { of, Subject } from 'rxjs';
import { AppError } from '../../../core/models/app-error';
import { ProdutoApiService } from '../data-access/produto-api.service';
import type { ProdutoResponse } from '../models/produto-response';
import { ProductPricePage } from './product-price.page';

describe('ProductPricePage', () => {
  const product: ProdutoResponse = { id: 15, codigoBarras: '00123', descricao: 'Produto', precoVenda: 9.5 };
  const api = jasmine.createSpyObj<ProdutoApiService>('api', ['buscarPorCodigo', 'atualizarPreco']);
  const router = jasmine.createSpyObj<Router>('router', ['navigateByUrl']);
  const present = jasmine.createSpy().and.resolveTo();
  const toasts = { create: jasmine.createSpy().and.resolveTo({ present }) };

  beforeEach(() => {
    Object.defineProperty(router, 'events', { value: of(new NavigationEnd(1, '/produto/00123', '/produto/00123')), configurable: true });
    api.buscarPorCodigo.and.returnValue(of(product));
    api.atualizarPreco.and.returnValue(of(undefined));
    router.navigateByUrl.and.resolveTo(true);
    TestBed.configureTestingModule({
      imports: [ProductPricePage],
      providers: [provideIonicAngular(),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ codigo: '00123' }) } } },
        { provide: ProdutoApiService, useValue: api },
        { provide: Router, useValue: router },
        { provide: ToastController, useValue: toasts }]
    });
  });

  afterEach(() => {
    api.buscarPorCodigo.calls.reset();
    api.atualizarPreco.calls.reset();
    router.navigateByUrl.calls.reset();
  });

  it('consulta usando o código da rota e encerra loading', () => {
    const fixture = TestBed.createComponent(ProductPricePage);
    fixture.detectChanges();
    expect(api.buscarPorCodigo).toHaveBeenCalledWith('00123');
    expect(fixture.componentInstance.product()?.id).toBe(15);
    expect(fixture.componentInstance.loading()).toBeFalse();
  });

  it('valida o preço e atualiza pelo ID correto', fakeAsync(() => {
    const page = TestBed.createComponent(ProductPricePage).componentInstance;
    page.ngOnInit();
    page.form.controls.novoPreco.setValue('10,50');
    page.save();
    flushMicrotasks();
    expect(api.atualizarPreco).toHaveBeenCalledOnceWith(15, 10.5);
  }));

  it('bloqueia envio duplo enquanto a primeira requisição está ativa', () => {
    const pending = new Subject<void>();
    api.atualizarPreco.and.returnValue(pending);
    const page = TestBed.createComponent(ProductPricePage).componentInstance;
    page.ngOnInit();
    page.form.controls.novoPreco.setValue('10');
    page.save();
    page.save();
    expect(api.atualizarPreco).toHaveBeenCalledTimes(1);
    pending.error(new AppError(503, 'Servidor indisponível.'));
    expect(page.errorMessage()).toBe('Servidor indisponível.');
    expect(router.navigateByUrl).not.toHaveBeenCalled();
  });

  it('não envia formulário inválido', () => {
    const page = TestBed.createComponent(ProductPricePage).componentInstance;
    page.ngOnInit();
    page.form.controls.novoPreco.setValue('0');
    page.save();
    expect(api.atualizarPreco).not.toHaveBeenCalled();
  });

  it('opera teclado, formata preço e cancela', () => {
    const page = TestBed.createComponent(ProductPricePage).componentInstance;
    page.appendKey('1');
    page.appendKey(',');
    page.appendKey('5');
    page.appendKey('backspace');
    expect(page.form.controls.novoPreco.value).toBe('1,');
    expect(page.formatPrice(9.5)).toContain('9,50');
    page.cancel();
    expect(router.navigateByUrl).toHaveBeenCalledWith('/scanner');
  });
});
