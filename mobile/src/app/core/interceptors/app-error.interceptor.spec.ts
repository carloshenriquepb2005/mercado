import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import type { AppError } from '../models/app-error';
import { appErrorInterceptor } from './app-error.interceptor';

describe('appErrorInterceptor', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(withInterceptors([appErrorInterceptor])), provideHttpClientTesting()]
    });
    http = TestBed.inject(HttpTestingController);
  });

  for (const [status, expected] of [
    [0, 'Não foi possível conectar ao servidor.'],
    [400, 'Confira os dados informados e tente novamente.'],
    [401, 'Usuário ou senha inválidos.'],
    [404, 'Produto não encontrado.'],
    [500, 'Ocorreu um erro inesperado.'],
    [503, 'Servidor ou banco de dados indisponível.']
  ] as const) {
    it(`mapeia status ${status}`, () => {
      TestBed.inject(HttpClient).get('/test').subscribe({
        error: (error: AppError) => expect(error.userMessage).toBe(expected)
      });
      http.expectOne('/test').flush(null, { status, statusText: 'Erro' });
    });
  }
});
