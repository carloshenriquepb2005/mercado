import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, NavigationEnd, Router } from '@angular/router';
import { provideIonicAngular } from '@ionic/angular/standalone';
import { of, throwError } from 'rxjs';
import { AppError } from '../../../../core/http/app-error';
import { ProdutoApiService } from '../../data-access/produto-api.service';
import type { ProdutoResponse } from '../../models/produto-response';
import { ProductStockPage } from './product-stock.page';

describe('ProductStockPage', () => {
  const product: ProdutoResponse = {
    id: 15,
    codigoBarras: '00123',
    descricao: 'Arroz integral',
    unidade: 'UN',
    precoVenda: 9.5,
    codigoNcm: '10063021',
    estoqueAtual: 12.5
  };
  const api = jasmine.createSpyObj<ProdutoApiService>('api', ['buscarPorCodigo', 'atualizarPreco']);
  const router = jasmine.createSpyObj<Router>('router', ['navigateByUrl']);

  beforeEach(() => {
    Object.defineProperty(router, 'events', {
      value: of(new NavigationEnd(1, '/consulta-estoque/00123', '/consulta-estoque/00123')),
      configurable: true
    });
    api.buscarPorCodigo.and.returnValue(of(product));
    router.navigateByUrl.and.resolveTo(true);
    TestBed.configureTestingModule({
      imports: [ProductStockPage],
      providers: [provideIonicAngular(),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ codigo: '00123' }) } } },
        { provide: ProdutoApiService, useValue: api },
        { provide: Router, useValue: router }]
    });
  });

  afterEach(() => {
    api.buscarPorCodigo.calls.reset();
    api.atualizarPreco.calls.reset();
    router.navigateByUrl.calls.reset();
  });

  it('exibe os dados de estoque sem uma linha independente de unidade', () => {
    const fixture = TestBed.createComponent(ProductStockPage);
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('00123');
    expect(text).toContain('Arroz integral');
    expect(text).toContain('UN');
    expect(text).toContain('9,50');
    expect(text).toContain('10063021');
    expect(text).toContain('12.5 UN');
    const labels = Array.from(fixture.nativeElement.querySelectorAll('dt'))
      .map((label) => (label as HTMLElement).textContent?.trim());
    expect(labels).not.toContain('Unidade');
    expect(fixture.nativeElement.querySelector('form')).toBeNull();
    expect(api.atualizarPreco).not.toHaveBeenCalled();
  });

  it('faz o botão de nova consulta ocupar a mesma largura do card', () => {
    const fixture = TestBed.createComponent(ProductStockPage);
    fixture.detectChanges();
    const button = fixture.nativeElement.querySelector('.stock-action') as HTMLElement;
    expect(button).not.toBeNull();
    expect(button.getAttribute('expand')).toBe('block');
  });

  it('apresenta erro amigável quando a consulta falha', () => {
    api.buscarPorCodigo.and.returnValue(throwError(() => new AppError(404, 'Produto não encontrado.')));
    const fixture = TestBed.createComponent(ProductStockPage);
    fixture.detectChanges();
    expect(fixture.componentInstance.errorMessage()).toBe('Produto não encontrado.');
  });

  it('volta ao scanner de consulta', () => {
    const page = TestBed.createComponent(ProductStockPage).componentInstance;
    page.scanAnother();
    expect(router.navigateByUrl).toHaveBeenCalledWith('/scanner/consulta');
  });
});
