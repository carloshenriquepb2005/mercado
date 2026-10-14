import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import type { Observable } from 'rxjs';
import type { AuthUser } from '../../../core/auth/auth-user';
import { ApiConfigService } from '../../../core/config/api-config.service';

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(ApiConfigService);

  login(username: string, password: string): Observable<AuthUser> {
    return this.http.post<AuthUser>(`${this.config.baseUrl()}/api/auth/login`, { username, password });
  }
}
