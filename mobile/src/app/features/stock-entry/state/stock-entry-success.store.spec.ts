import { TestBed } from '@angular/core/testing';
import { StockEntrySuccessStore } from './stock-entry-success.store';
import type { StockEntrySuccessData } from './stock-entry-success.store';

describe('StockEntrySuccessStore', () => {
  const data: StockEntrySuccessData = {
    description: 'Produto', barcode: '00123', unit: 'UN',
    previousStock: 18.5, currentStock: 31, previousCost: null, currentCost: 7.35
  };

  beforeEach(() => sessionStorage.clear());
  afterEach(() => sessionStorage.clear());

  it('guarda e limpa os dados da última entrada', () => {
    const store = TestBed.inject(StockEntrySuccessStore);
    store.set(data);
    expect(store.data()).toEqual(data);
    store.clear();
    expect(store.data()).toBeNull();
    expect(sessionStorage.getItem('precificacao.lastStockEntry')).toBeNull();
  });

  it('restaura os dados válidos da sessão, inclusive custo anterior nulo', () => {
    sessionStorage.setItem('precificacao.lastStockEntry', JSON.stringify(data));
    expect(TestBed.inject(StockEntrySuccessStore).data()).toEqual(data);
  });

  it('ignora dados inválidos ou corrompidos', () => {
    sessionStorage.setItem('precificacao.lastStockEntry', JSON.stringify({ description: 'x' }));
    expect(TestBed.inject(StockEntrySuccessStore).data()).toBeNull();
  });

  it('ignora JSON corrompido', () => {
    TestBed.resetTestingModule();
    sessionStorage.setItem('precificacao.lastStockEntry', '{quebrado');
    expect(TestBed.inject(StockEntrySuccessStore).data()).toBeNull();
  });
});
