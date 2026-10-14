import { HttpErrorResponse, type HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { AppError } from './app-error';

const messages: Readonly<Record<number, string>> = {
  0: 'Não foi possível conectar ao servidor.',
  400: 'Confira os dados informados e tente novamente.',
  401: 'Usuário ou senha inválidos.',
  404: 'Produto não encontrado.',
  500: 'Ocorreu um erro inesperado.',
  503: 'Servidor ou banco de dados indisponível.'
};

export const appErrorInterceptor: HttpInterceptorFn = (request, next) =>
  next(request).pipe(
    catchError((error: unknown) => {
      if (error instanceof AppError) return throwError(() => error);
      const status = error instanceof HttpErrorResponse ? error.status : 500;
      return throwError(() => new AppError(status, messages[status] ?? messages[500]));
    })
  );
