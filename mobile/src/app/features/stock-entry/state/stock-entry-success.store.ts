import { Injectable, signal } from '@angular/core';

export interface StockEntrySuccessData {
  description: string;
  barcode: string;
  unit: string | null;
  previousStock: number;
  currentStock: number;
  previousCost: number | null;
  currentCost: number;
}

const SUCCESS_KEY = 'precificacao.lastStockEntry';

@Injectable({ providedIn: 'root' })
export class StockEntrySuccessStore {
  private readonly value = signal<StockEntrySuccessData | null>(this.restore());
  readonly data = this.value.asReadonly();

  set(data: StockEntrySuccessData): void {
    this.value.set(data);
    try { sessionStorage.setItem(SUCCESS_KEY, JSON.stringify(data)); } catch { /* armazenamento indisponível */ }
  }

  clear(): void {
    this.value.set(null);
    try { sessionStorage.removeItem(SUCCESS_KEY); } catch { /* armazenamento indisponível */ }
  }

  private restore(): StockEntrySuccessData | null {
    try {
      const stored = sessionStorage.getItem(SUCCESS_KEY);
      if (!stored) return null;
      const data = JSON.parse(stored) as Partial<StockEntrySuccessData>;
      return typeof data.description === 'string'
        && typeof data.barcode === 'string'
        && typeof data.previousStock === 'number'
        && typeof data.currentStock === 'number'
        && typeof data.currentCost === 'number'
        && (data.previousCost === null || typeof data.previousCost === 'number')
        ? data as StockEntrySuccessData
        : null;
    } catch {
      return null;
    }
  }
}
