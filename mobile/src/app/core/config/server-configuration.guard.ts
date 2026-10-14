import { inject } from '@angular/core';
import type { CanActivateFn } from '@angular/router';
import { Router } from '@angular/router';
import { AuthSessionService } from '../auth/auth-session.service';
import { ApiConfigService } from './api-config.service';
import { SettingsAccessService } from './settings-access.service';

export const serverConfigurationGuard: CanActivateFn = () => {
  const config = inject(ApiConfigService);
  if (!config.hasSavedConfiguration()) return true;

  const session = inject(AuthSessionService);
  const router = inject(Router);
  if (!session.isAuthenticated()) return router.createUrlTree(['/login']);

  return inject(SettingsAccessService).consume()
    ? true
    : router.createUrlTree(['/ajustes']);
};
