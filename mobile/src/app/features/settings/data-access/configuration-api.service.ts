import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import type { Observable } from 'rxjs';
import { timeout } from 'rxjs';
import { ApiConfigService } from '../../../core/config/api-config.service';

@Injectable({ providedIn: 'root' })
export class ConfigurationApiService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(ApiConfigService);

  testConnection(ip: string): Observable<void> {
    return this.http.get<void>(`${this.config.urlForIp(ip)}/api/configuracoes/conexao`).pipe(
      timeout(7000)
    );
  }

  validateCashierPassword(password: string): Observable<void> {
    return this.http.post<void>(`${this.config.baseUrl()}/api/configuracoes/validar-senha-caixa`, {
      senha: password
    });
  }
}
