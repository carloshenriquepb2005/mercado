import { TestBed } from '@angular/core/testing';
import type { ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Router } from '@angular/router';
import { AuthSessionService } from '../auth/auth-session.service';
import { ApiConfigService } from './api-config.service';
import { configurationGuard } from './configuration.guard';
import { serverConfigurationGuard } from './server-configuration.guard';
import { SettingsAccessService } from './settings-access.service';

describe('configuration guards', () => {
  const config = jasmine.createSpyObj<ApiConfigService>('config', ['hasSavedConfiguration']);
  const session = jasmine.createSpyObj<AuthSessionService>('session', ['isAuthenticated']);
  const access = jasmine.createSpyObj<SettingsAccessService>('access', ['consume']);
  const loginTree = {} as UrlTree;
  const settingsTree = {} as UrlTree;
  const configurationTree = {} as UrlTree;
  const router = jasmine.createSpyObj<Router>('router', ['createUrlTree']);
  const route = {} as ActivatedRouteSnapshot;
  const state = {} as RouterStateSnapshot;

  beforeEach(() => {
    router.createUrlTree.and.callFake((commands: unknown[]) => {
      if (commands[0] === '/login') return loginTree;
      if (commands[0] === '/ajustes') return settingsTree;
      return configurationTree;
    });
    TestBed.configureTestingModule({ providers: [
      { provide: ApiConfigService, useValue: config },
      { provide: AuthSessionService, useValue: session },
      { provide: SettingsAccessService, useValue: access },
      { provide: Router, useValue: router }
    ] });
  });

  afterEach(() => {
    config.hasSavedConfiguration.calls.reset();
    session.isAuthenticated.calls.reset();
    access.consume.calls.reset();
    router.createUrlTree.calls.reset();
  });

  it('redireciona ao cadastro inicial enquanto não houver servidor salvo', () => {
    config.hasSavedConfiguration.and.returnValue(false);
    const result = TestBed.runInInjectionContext(() => configurationGuard(route, state));
    expect(result).toBe(configurationTree);
  });

  it('libera as rotas normais quando houver configuração', () => {
    config.hasSavedConfiguration.and.returnValue(true);
    const result = TestBed.runInInjectionContext(() => configurationGuard(route, state));
    expect(result).toBeTrue();
  });

  it('permite a configuração inicial e protege a reconfiguração', () => {
    config.hasSavedConfiguration.and.returnValue(false);
    expect(TestBed.runInInjectionContext(() => serverConfigurationGuard(route, state))).toBeTrue();

    config.hasSavedConfiguration.and.returnValue(true);
    session.isAuthenticated.and.returnValue(false);
    expect(TestBed.runInInjectionContext(() => serverConfigurationGuard(route, state))).toBe(loginTree);

    session.isAuthenticated.and.returnValue(true);
    access.consume.and.returnValue(false);
    expect(TestBed.runInInjectionContext(() => serverConfigurationGuard(route, state))).toBe(settingsTree);

    access.consume.and.returnValue(true);
    expect(TestBed.runInInjectionContext(() => serverConfigurationGuard(route, state))).toBeTrue();
  });
});
