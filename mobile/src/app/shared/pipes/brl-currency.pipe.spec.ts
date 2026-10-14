import { BrlCurrencyPipe } from './brl-currency.pipe';

describe('BrlCurrencyPipe', () => {
  it('formata valores em reais', () => {
    expect(new BrlCurrencyPipe().transform(10.5)).toContain('10,50');
  });
});
