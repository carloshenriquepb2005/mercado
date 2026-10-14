import { provideHttpClient, withInterceptors } from '@angular/common/http';
import type { ApplicationConfig } from '@angular/core';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter, RouteReuseStrategy, withComponentInputBinding } from '@angular/router';
import { BarcodeScanner } from '@capacitor-mlkit/barcode-scanning';
import { IonicRouteStrategy, provideIonicAngular } from '@ionic/angular/standalone';
import { API_BASE_URL, apiBaseUrlFactory } from './core/config/api.config';
import { appErrorInterceptor } from './core/http/app-error.interceptor';
import { routes } from './app.routes';
import { BARCODE_SCANNER } from './features/scanner/data-access/barcode-scanner.port';

export const appConfig: ApplicationConfig = {
  providers: [
    provideIonicAngular({ mode: 'md' }),
    provideAnimationsAsync(),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withInterceptors([appErrorInterceptor])),
    { provide: RouteReuseStrategy, useClass: IonicRouteStrategy },
    { provide: API_BASE_URL, useFactory: apiBaseUrlFactory },
    { provide: BARCODE_SCANNER, useValue: BarcodeScanner }
  ]
};
