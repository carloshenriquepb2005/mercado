import { InjectionToken } from '@angular/core';
import { environment } from '../../../environments/environment.generated';

export const API_BASE_URL = new InjectionToken<string>('API_BASE_URL');

export function apiBaseUrlFactory(): string {
  return environment.apiUrl.replace(/\/$/, '');
}

