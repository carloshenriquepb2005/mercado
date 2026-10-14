import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { catchError, map, of, timeout } from 'rxjs';
import { ApiConfigService } from '../config/api-config.service';

export type ApiConnectionState = 'checking' | 'online' | 'offline';

@Injectable({ providedIn: 'root' })
export class ConnectivityService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(ApiConfigService);
  readonly state = signal<ApiConnectionState>('checking');

  check() {
    this.state.set('checking');
    const apiUrl = this.config.baseUrl();
    if (!apiUrl) {
      this.state.set('offline');
      return of(false);
    }
    return this.http.get<{ status: string }>(`${apiUrl}/actuator/health`).pipe(
      timeout(4000),
      map((response) => response.status === 'UP'),
      catchError(() => of(false)),
      map((available) => {
        this.state.set(available ? 'online' : 'offline');
        return available;
      })
    );
  }
}

