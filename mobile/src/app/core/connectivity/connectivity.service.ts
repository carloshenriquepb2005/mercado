import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { catchError, map, of, timeout } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';

export type ApiConnectionState = 'checking' | 'online' | 'offline';

@Injectable({ providedIn: 'root' })
export class ConnectivityService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = inject(API_BASE_URL);
  readonly state = signal<ApiConnectionState>('checking');

  check() {
    this.state.set('checking');
    return this.http.get<{ status: string }>(`${this.apiUrl}/actuator/health`).pipe(
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

