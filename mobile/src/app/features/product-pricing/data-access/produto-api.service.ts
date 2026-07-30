import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import type { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/config/api.config';
import type { ProdutoResponse } from '../models/produto-response';

@Injectable({ providedIn: 'root' })
export class ProdutoApiService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = inject(API_BASE_URL);

  buscarPorCodigo(ean: string): Observable<ProdutoResponse> {
    return this.http.get<ProdutoResponse>(`${this.apiUrl}/api/produtos/${encodeURIComponent(ean)}`);
  }

  atualizarPreco(produtoId: number, novoPreco: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/api/produtos/${produtoId}`, { novoPreco });
  }
}

