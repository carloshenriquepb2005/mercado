import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import type { Observable } from 'rxjs';
import { ApiConfigService } from '../../../core/config/api-config.service';
import type { ProdutoResponse } from '../models/produto-response';

@Injectable({ providedIn: 'root' })
export class ProdutoApiService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(ApiConfigService);

  buscarPorCodigo(ean: string): Observable<ProdutoResponse> {
    return this.http.get<ProdutoResponse>(`${this.config.baseUrl()}/api/produtos/${encodeURIComponent(ean)}`);
  }

  atualizarPreco(produtoId: number, novoPreco: number): Observable<void> {
    return this.http.patch<void>(`${this.config.baseUrl()}/api/produtos/${produtoId}`, { novoPreco });
  }
}

