import { Injectable, signal } from '@angular/core';
import { normalizeIpv4 } from './ip-address.util';

const API_URL_KEY = 'precificacao.config.apiUrl';
const API_PORT = 8080;

@Injectable({ providedIn: 'root' })
export class ApiConfigService {
  private readonly storedUrl = signal<string | null>(this.restore());
  readonly apiUrl = this.storedUrl.asReadonly();

  hasSavedConfiguration(): boolean {
    return this.storedUrl() !== null;
  }

  currentIp(): string {
    const value = this.storedUrl();
    if (!value) return '';
    try {
      return new URL(value).hostname;
    } catch {
      return '';
    }
  }

  baseUrl(): string {
    return this.storedUrl() ?? '';
  }

  urlForIp(ip: string): string {
    return `http://${ip}:${API_PORT}`;
  }

  save(ip: string): void {
    const url = this.urlForIp(ip);
    this.storedUrl.set(url);
    try { localStorage.setItem(API_URL_KEY, url); } catch { /* armazenamento indisponível */ }
  }

  private restore(): string | null {
    try {
      const value = localStorage.getItem(API_URL_KEY);
      if (!value) return null;
      const parsed = new URL(value);
      return parsed.protocol === 'http:' && parsed.port === String(API_PORT)
        && normalizeIpv4(parsed.hostname) !== null
        ? value.replace(/\/$/, '')
        : null;
    } catch {
      return null;
    }
  }
}
