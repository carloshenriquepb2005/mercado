import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ApiConfigService } from '../../../core/config/api-config.service';
import { ProdutoApiService } from './produto-api.service';

describe('ProdutoApiService', () => {
  let service: ProdutoApiService;
  let http: HttpTestingController;
  const config = jasmine.createSpyObj<ApiConfigService>('config', ['baseUrl']);

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(),
        { provide: ApiConfigService, useValue: config }]
    });
    config.baseUrl.and.returnValue('http://servidor:8080');
    service = TestBed.inject(ProdutoApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('faz GET com EAN codificado', () => {
    service.buscarPorCodigo('001 23').subscribe((product) => expect(product.id).toBe(1));
    const request = http.expectOne('http://servidor:8080/api/produtos/001%2023');
    expect(request.request.method).toBe('GET');
    request.flush({
      id: 1,
      codigoBarras: '00123',
      descricao: 'Produto',
      unidade: 'UN',
      precoVenda: 9.5,
      precoCusto: 6.9,
      codigoNcm: '12345678',
      estoqueAtual: 12.5
    });
  });

  it('faz PATCH pelo ID e com o corpo correto', () => {
    service.atualizarPreco(15, 10.5).subscribe((response) => expect(response).toBeNull());
    const request = http.expectOne('http://servidor:8080/api/produtos/15');
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({ novoPreco: 10.5 });
    request.flush(null, { status: 204, statusText: 'No Content' });
  });

  it('faz POST da entrada de estoque pelo ID, enviando a data de vencimento quando informada', () => {
    const entrada = {
      produtoId: 15, estoqueAnterior: 18.5, estoqueAtual: 31, precoCustoAnterior: 6.9, precoCustoAtual: 7.35
    };
    service.registrarEntradaEstoque(15, { quantidade: 12.5, precoCusto: 7.35, dataVencimento: '2027-03-15' })
      .subscribe((response) => expect(response).toEqual(entrada));
    const request = http.expectOne('http://servidor:8080/api/produtos/15/entrada-estoque');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ quantidade: 12.5, precoCusto: 7.35, dataVencimento: '2027-03-15' });
    request.flush(entrada);
  });

  it('omite dataVencimento do corpo quando não informada (nunca envia null ou vazio)', () => {
    service.registrarEntradaEstoque(15, { quantidade: 1, precoCusto: 2, dataVencimento: '' }).subscribe();
    const request = http.expectOne('http://servidor:8080/api/produtos/15/entrada-estoque');
    expect(request.request.body).toEqual({ quantidade: 1, precoCusto: 2 });
    expect('dataVencimento' in request.request.body).toBeFalse();
    request.flush({ produtoId: 15, estoqueAnterior: 0, estoqueAtual: 1, precoCustoAnterior: null, precoCustoAtual: 2 });
  });

  it('propaga erro HTTP', () => {
    let status = 0;
    service.buscarPorCodigo('999').subscribe({ error: (error) => status = error.status });
    http.expectOne('http://servidor:8080/api/produtos/999')
      .flush({}, { status: 404, statusText: 'Not Found' });
    expect(status).toBe(404);
  });
});

