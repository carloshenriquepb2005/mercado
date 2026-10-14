import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ApiConfigService } from '../../../core/config/api-config.service';
import { AuthApiService } from './auth-api.service';

describe('AuthApiService', () => {
  let service: AuthApiService;
  let http: HttpTestingController;
  const config = jasmine.createSpyObj<ApiConfigService>('config', ['baseUrl']);

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(),
        { provide: ApiConfigService, useValue: config }]
    });
    config.baseUrl.and.returnValue('http://servidor:8080');
    service = TestBed.inject(AuthApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('envia as credenciais ao endpoint de login', () => {
    service.login('carlos', '123').subscribe((user) => expect(user.displayName).toBe('Carlos'));
    const request = http.expectOne('http://servidor:8080/api/auth/login');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ username: 'carlos', password: '123' });
    request.flush({ codigo: 7, username: 'Carlos', displayName: 'Carlos', perfil: 2 });
  });
});
