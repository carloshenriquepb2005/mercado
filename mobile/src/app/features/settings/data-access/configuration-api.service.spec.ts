import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { fakeAsync, tick } from '@angular/core/testing';
import { ApiConfigService } from '../../../core/config/api-config.service';
import { ConfigurationApiService } from './configuration-api.service';

describe('ConfigurationApiService', () => {
  const config = jasmine.createSpyObj<ApiConfigService>('config', ['urlForIp', 'baseUrl']);
  let service: ConfigurationApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    config.urlForIp.and.returnValue('http://192.168.0.10:8080');
    config.baseUrl.and.returnValue('http://servidor:8080');
    TestBed.configureTestingModule({ providers: [
      provideHttpClient(), provideHttpClientTesting(),
      { provide: ApiConfigService, useValue: config }
    ] });
    service = TestBed.inject(ConfigurationApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('testa a conexão usando o IP ainda não salvo', () => {
    service.testConnection('192.168.0.10').subscribe();
    const request = http.expectOne('http://192.168.0.10:8080/api/configuracoes/conexao');
    expect(request.request.method).toBe('GET');
    request.flush(null, { status: 204, statusText: 'No Content' });
  });

  it('interrompe o teste de conexão depois de sete segundos', fakeAsync(() => {
    let failed = false;
    service.testConnection('192.168.0.10').subscribe({ error: () => failed = true });
    const request = http.expectOne('http://192.168.0.10:8080/api/configuracoes/conexao');

    tick(6999);
    expect(failed).toBeFalse();
    tick(1);
    expect(failed).toBeTrue();
    expect(request.cancelled).toBeTrue();
  }));

  it('valida a senha no servidor atualmente configurado', () => {
    service.validateCashierPassword('1234').subscribe();
    const request = http.expectOne('http://servidor:8080/api/configuracoes/validar-senha-caixa');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ senha: '1234' });
    request.flush(null, { status: 204, statusText: 'No Content' });
  });
});
