import type { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/pages/login.page').then((m) => m.LoginPage)
  },
  {
    path: 'home',
    canActivate: [authGuard],
    loadComponent: () => import('./features/home/pages/home.page').then((m) => m.HomePage)
  },
  {
    path: 'scanner',
    canActivate: [authGuard],
    loadComponent: () => import('./features/scanner/pages/scanner.page').then((m) => m.ScannerPage)
  },
  {
    path: 'produto/:codigo',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/product-pricing/pages/product-price.page').then((m) => m.ProductPricePage)
  },
  {
    path: 'sucesso',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/product-pricing/pages/pricing-success.page').then((m) => m.PricingSuccessPage)
  },
  { path: '**', redirectTo: 'login' }
];
