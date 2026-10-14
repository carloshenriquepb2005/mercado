import type { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { configurationGuard } from './core/config/configuration.guard';
import { serverConfigurationGuard } from './core/config/server-configuration.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  {
    path: 'configuracao-servidor',
    canActivate: [serverConfigurationGuard],
    loadComponent: () =>
      import('./features/settings/pages/server-configuration/server-configuration.page')
        .then((m) => m.ServerConfigurationPage)
  },
  {
    path: 'login',
    canActivate: [configurationGuard],
    loadComponent: () => import('./features/auth/pages/login/login.page').then((m) => m.LoginPage)
  },
  {
    path: 'home',
    canActivate: [configurationGuard, authGuard],
    loadComponent: () => import('./features/home/pages/home/home.page').then((m) => m.HomePage)
  },
  {
    path: 'scanner',
    canActivate: [configurationGuard, authGuard],
    loadComponent: () => import('./features/scanner/pages/scanner/scanner.page').then((m) => m.ScannerPage)
  },
  {
    path: 'produto/:codigo',
    canActivate: [configurationGuard, authGuard],
    loadComponent: () =>
      import('./features/product-pricing/pages/product-price/product-price.page').then((m) => m.ProductPricePage)
  },
  {
    path: 'sucesso',
    canActivate: [configurationGuard, authGuard],
    loadComponent: () =>
      import('./features/product-pricing/pages/pricing-success/pricing-success.page').then((m) => m.PricingSuccessPage)
  },
  {
    path: 'ajustes',
    canActivate: [configurationGuard, authGuard],
    loadComponent: () =>
      import('./features/settings/pages/settings/settings.page').then((m) => m.SettingsPage)
  },
  { path: '**', redirectTo: 'login' }
];
