import { TestBed } from '@angular/core/testing';
import { NavigationEnd, Router } from '@angular/router';
import { provideIonicAngular } from '@ionic/angular/standalone';
import { of, throwError } from 'rxjs';
import { AuthSessionService } from '../../../../core/auth/auth-session.service';
import { ApiConfigService } from '../../../../core/config/api-config.service';
import { SettingsAccessService } from '../../../../core/config/settings-access.service';
import { NotificationService } from '../../../../core/feedback/notification.service';
import { ConfigurationApiService } from '../../data-access/configuration-api.service';
import { ServerConfigurationPage } from './server-configuration.page';

describe('ServerConfigurationPage', () => {
  const api = jasmine.createSpyObj<ConfigurationApiService>('api', ['testConnection']);
  const config = jasmine.createSpyObj<ApiConfigService>(
    'config', ['hasSavedConfiguration', 'currentIp', 'save']);
  const access = jasmine.createSpyObj<SettingsAccessService>('access', ['lock']);
  const session = jasmine.createSpyObj<AuthSessionService>('session', ['end']);
  const router = jasmine.createSpyObj<Router>('router', ['navigateByUrl']);
  const notifications = jasmine.createSpyObj<NotificationService>('notifications', ['show']);

  beforeEach(() => {
    config.hasSavedConfiguration.and.returnValue(false);
    config.currentIp.and.returnValue('');
    api.testConnection.and.returnValue(of(undefined));
    router.navigateByUrl.and.resolveTo(true);
    Object.defineProperty(router, 'events', {
      value: of(new NavigationEnd(1, '/configuracao-servidor', '/configuracao-servidor')),
      configurable: true
    });
    notifications.show.and.resolveTo();
    TestBed.configureTestingModule({
      imports: [ServerConfigurationPage],
      providers: [provideIonicAngular(),
        { provide: ConfigurationApiService, useValue: api },
        { provide: ApiConfigService, useValue: config },
        { provide: SettingsAccessService, useValue: access },
        { provide: AuthSessionService, useValue: session },
        { provide: Router, useValue: router },
        { provide: NotificationService, useValue: notifications }]
    });
  });

  afterEach(() => {
    api.testConnection.calls.reset();
    config.save.calls.reset();
    access.lock.calls.reset();
    session.end.calls.reset();
    router.navigateByUrl.calls.reset();
  });

  it('habilita salvamento e login somente depois do teste com sucesso', async () => {
    const page = TestBed.createComponent(ServerConfigurationPage).componentInstance;
    page.updateIp({ target: { value: '192.168.001.010' } } as unknown as Event);
    expect(page.canProceed()).toBeFalse();

    page.testConnection();
    expect(api.testConnection).toHaveBeenCalledWith('192.168.1.10');
    expect(page.canProceed()).toBeTrue();
    expect(page.result()).toBe('success');

    await page.save();
    expect(config.save).toHaveBeenCalledWith('192.168.1.10');
    expect(notifications.show).toHaveBeenCalledWith(
      'Configuração salva com sucesso!', { color: 'success' });
    await page.goToLogin();
    expect(session.end).toHaveBeenCalled();
    expect(router.navigateByUrl).toHaveBeenCalledWith('/login', { replaceUrl: true });
  });

  it('mantém o acesso bloqueado para IP inválido ou falha de conexão', () => {
    const page = TestBed.createComponent(ServerConfigurationPage).componentInstance;
    page.updateIp({ target: { value: '300.1.1.1' } } as unknown as Event);
    page.testConnection();
    expect(page.invalidIp()).toBeTrue();
    expect(api.testConnection).not.toHaveBeenCalled();

    api.testConnection.and.returnValue(throwError(() => new Error('offline')));
    page.updateIp({ target: { value: '10.0.0.8' } } as unknown as Event);
    page.testConnection();
    expect(page.result()).toBe('failure');
    expect(page.canProceed()).toBeFalse();
  });

  it('invalida o teste anterior ao editar ou limpar o IP', () => {
    const page = TestBed.createComponent(ServerConfigurationPage).componentInstance;
    page.updateIp({ target: { value: '10.0.0.8' } } as unknown as Event);
    page.testConnection();
    expect(page.canProceed()).toBeTrue();
    page.clearIp();
    expect(page.canProceed()).toBeFalse();
    expect(page.result()).toBe('none');
  });

  it('verifica o IP salvo automaticamente e oculta o login no acesso interno', () => {
    config.hasSavedConfiguration.and.returnValue(true);
    config.currentIp.and.returnValue('10.0.0.8');
    const fixture = TestBed.createComponent(ServerConfigurationPage);

    fixture.detectChanges();

    expect(api.testConnection).toHaveBeenCalledWith('10.0.0.8');
    expect(fixture.componentInstance.connectionState()).toBe('online');
    expect(fixture.nativeElement.querySelector('.login-footer')).toBeNull();
  });

  it('mantém o botão de login na configuração inicial', () => {
    const fixture = TestBed.createComponent(ServerConfigurationPage);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.login-footer')).not.toBeNull();
    expect(api.testConnection).not.toHaveBeenCalled();
  });
});
