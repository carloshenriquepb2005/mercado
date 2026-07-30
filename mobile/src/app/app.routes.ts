import type { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'scanner' },
  {
    path: 'scanner',
    loadComponent: () => import('./features/scanner/pages/scanner.page').then((m) => m.ScannerPage)
  },
  {
    path: 'produto/:codigo',
    loadComponent: () =>
      import('./features/product-pricing/pages/product-price.page').then((m) => m.ProductPricePage)
  },
  { path: '**', redirectTo: 'scanner' }
];

