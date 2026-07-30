import { FormControl } from '@angular/forms';
import { priceValidator } from './price.validator';

describe('priceValidator', () => {
  it('aceita valores válidos e nulo para o required tratar', () => {
    expect(priceValidator(new FormControl('10,50'))).toBeNull();
    expect(priceValidator(new FormControl('99999999.99'))).toBeNull();
    expect(priceValidator(new FormControl(''))).toBeNull();
  });

  it('rejeita zero, negativo, excesso de casas e limite', () => {
    for (const value of ['0', '-1', '1,001', '100000000', 'texto']) {
      expect(priceValidator(new FormControl(value))).toEqual({ price: true });
    }
  });
});

