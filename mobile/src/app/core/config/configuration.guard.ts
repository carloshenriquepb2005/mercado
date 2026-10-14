import { inject } from '@angular/core';
import type { CanActivateFn } from '@angular/router';
import { Router } from '@angular/router';
import { ApiConfigService } from './api-config.service';

export const configurationGuard: CanActivateFn = () => {
  const config = inject(ApiConfigService);
  return config.hasSavedConfiguration()
    ? true
    : inject(Router).createUrlTree(['/configuracao-servidor']);
};
