import { Injectable, signal } from '@angular/core';

export interface PricingSuccessData {
  description: string;
  barcode: string;
  previousPrice: number;
  newPrice: number;
}

const SUCCESS_KEY = 'precificacao.lastSuccess';

@Injectable({ providedIn: 'root' })
export class PricingSuccessService {
  private readonly value = signal<PricingSuccessData | null>(this.restore());
  readonly data = this.value.asReadonly();

  set(data: PricingSuccessData): void {
    this.value.set(data);
    try { sessionStorage.setItem(SUCCESS_KEY, JSON.stringify(data)); } catch { /* armazenamento indisponível */ }
  }

  clear(): void {
    this.value.set(null);
    try { sessionStorage.removeItem(SUCCESS_KEY); } catch { /* armazenamento indisponível */ }
  }

  private restore(): PricingSuccessData | null {
    try {
      const stored = sessionStorage.getItem(SUCCESS_KEY);
      if (!stored) return null;
      const data = JSON.parse(stored) as Partial<PricingSuccessData>;
      return typeof data.description === 'string'
        && typeof data.barcode === 'string'
        && typeof data.previousPrice === 'number'
        && typeof data.newPrice === 'number'
        ? data as PricingSuccessData
        : null;
    } catch {
      return null;
    }
  }
}
