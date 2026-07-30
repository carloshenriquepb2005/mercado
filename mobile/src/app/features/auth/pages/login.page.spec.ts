import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { provideIonicAngular, ToastController } from '@ionic/angular/standalone';
import { of, throwError } from 'rxjs';
import { AuthApiService } from '../../../core/auth/auth-api.service';
import { AuthSessionService } from '../../../core/auth/auth-session.service';
import { AppError } from '../../../core/models/app-error';
import { LoginPage } from './login.page';

describe('LoginPage', () => {
  const api = jasmine.createSpyObj<AuthApiService>('api', ['login']);
  const session = jasmine.createSpyObj<AuthSessionService>('session', ['rememberedUsername', 'start']);
  const router = jasmine.createSpyObj<Router>('router', ['navigateByUrl']);
  const present = jasmine.createSpy().and.resolveTo();
  const toasts = { create: jasmine.createSpy().and.resolveTo({ present }) };

  beforeEach(() => {
    session.rememberedUsername.and.returnValue('');
    api.login.and.returnValue(of({ codigo: 7, username: 'Carlos', displayName: 'Carlos', perfil: 2 }));
    router.navigateByUrl.and.resolveTo(true);
    TestBed.configureTestingModule({
      imports: [LoginPage],
      providers: [provideIonicAngular(),
        { provide: AuthApiService, useValue: api },
        { provide: AuthSessionService, useValue: session },
        { provide: Router, useValue: router },
        { provide: ToastController, useValue: toasts }]
    });
  });

  afterEach(() => {
    api.login.calls.reset();
    session.start.calls.reset();
    router.navigateByUrl.calls.reset();
  });

  it('autentica, inicia a sessão e abre a home', () => {
    const page = TestBed.createComponent(LoginPage).componentInstance;
    page.form.setValue({ username: ' carlos ', password: '123', remember: true });
    page.submit();
    expect(api.login).toHaveBeenCalledWith('carlos', '123');
    expect(session.start).toHaveBeenCalledWith(
      { codigo: 7, username: 'Carlos', displayName: 'Carlos', perfil: 2 }, true);
    expect(router.navigateByUrl).toHaveBeenCalledWith('/home', { replaceUrl: true });
  });

  it('mostra erro de credenciais e não navega', () => {
    api.login.and.returnValue(throwError(() => new AppError(401, 'Usuário ou senha inválidos.')));
    const page = TestBed.createComponent(LoginPage).componentInstance;
    page.form.setValue({ username: 'carlos', password: 'errada', remember: false });
    page.submit();
    expect(page.errorMessage()).toBe('Usuário ou senha inválidos.');
    expect(router.navigateByUrl).not.toHaveBeenCalled();
  });

  it('não envia campos vazios e alterna a visibilidade da senha', () => {
    const page = TestBed.createComponent(LoginPage).componentInstance;
    page.submit();
    expect(api.login).not.toHaveBeenCalled();
    page.togglePassword();
    expect(page.showPassword()).toBeTrue();
  });
});
