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
    request.flush({ id: 1, codigoBarras: '00123', descricao: 'Produto', precoVenda: 9.5 });
  });

  it('faz PATCH pelo ID e com o corpo correto', () => {
    service.atualizarPreco(15, 10.5).subscribe((response) => expect(response).toBeNull());
    const request = http.expectOne('http://servidor:8080/api/produtos/15');
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({ novoPreco: 10.5 });
    request.flush(null, { status: 204, statusText: 'No Content' });
  });

  it('propaga erro HTTP', () => {
    let status = 0;
    service.buscarPorCodigo('999').subscribe({ error: (error) => status = error.status });
    http.expectOne('http://servidor:8080/api/produtos/999')
      .flush({}, { status: 404, statusText: 'Not Found' });
    expect(status).toBe(404);
  });
});

