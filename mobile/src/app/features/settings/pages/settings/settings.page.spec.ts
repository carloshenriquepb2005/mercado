import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { provideIonicAngular } from '@ionic/angular/standalone';
import { of, throwError } from 'rxjs';
import { SettingsAccessService } from '../../../../core/config/settings-access.service';
import { ConfigurationApiService } from '../../data-access/configuration-api.service';
import { SettingsPage } from './settings.page';

describe('SettingsPage', () => {
  const api = jasmine.createSpyObj<ConfigurationApiService>('api', ['validateCashierPassword']);
  const access = jasmine.createSpyObj<SettingsAccessService>('access', ['unlock']);
  const router = jasmine.createSpyObj<Router>('router', ['navigateByUrl']);

  beforeEach(() => {
    api.validateCashierPassword.and.returnValue(of(undefined));
    router.navigateByUrl.and.resolveTo(true);
    TestBed.configureTestingModule({
      imports: [SettingsPage],
      providers: [provideIonicAngular(),
        { provide: ConfigurationApiService, useValue: api },
        { provide: SettingsAccessService, useValue: access },
        { provide: Router, useValue: router }]
    });
  });

  afterEach(() => {
    api.validateCashierPassword.calls.reset();
    access.unlock.calls.reset();
    router.navigateByUrl.calls.reset();
  });

  it('solicita a senha e libera a configuração quando estiver correta', () => {
    const page = TestBed.createComponent(SettingsPage).componentInstance;
    page.openServerConfiguration();
    page.password.setValue('1234');
    page.validatePassword();
    expect(api.validateCashierPassword).toHaveBeenCalledWith('1234');
    expect(access.unlock).toHaveBeenCalled();
    expect(router.navigateByUrl).toHaveBeenCalledWith('/configuracao-servidor');
  });

  it('não libera acesso com senha vazia ou incorreta', () => {
    const page = TestBed.createComponent(SettingsPage).componentInstance;
    page.openServerConfiguration();
    page.validatePassword();
    expect(api.validateCashierPassword).not.toHaveBeenCalled();

    api.validateCashierPassword.and.returnValue(throwError(() => new Error('inválida')));
    page.password.setValue('9999');
    page.validatePassword();
    expect(page.errorMessage()).toBe('Senha do caixa incorreta.');
    expect(access.unlock).not.toHaveBeenCalled();
  });

  it('permite fechar o diálogo e alternar a visibilidade', () => {
    const page = TestBed.createComponent(SettingsPage).componentInstance;
    page.openServerConfiguration();
    page.togglePassword();
    expect(page.showPassword()).toBeTrue();
    page.closeModal();
    expect(page.modalOpen()).toBeFalse();
  });
});
