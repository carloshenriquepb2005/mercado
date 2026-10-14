import { TestBed } from '@angular/core/testing';
import { PricingSuccessStore } from './pricing-success.store';

describe('PricingSuccessStore', () => {
  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({ providers: [PricingSuccessStore] });
  });

  afterEach(() => sessionStorage.clear());

  it('mantém e limpa o resumo da alteração', () => {
    const store = TestBed.inject(PricingSuccessStore);
    const data = { description: 'Produto', barcode: '00123', previousPrice: 9.5, newPrice: 10.5 };
    store.set(data);
    expect(store.data()).toEqual(data);
    store.clear();
    expect(store.data()).toBeNull();
  });
});
