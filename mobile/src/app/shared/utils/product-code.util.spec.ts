import { isValidProductCode } from './product-code.util';

describe('isValidProductCode', () => {
  it('aceita de um a treze dígitos, incluindo zeros à esquerda', () => {
    expect(isValidProductCode('0')).toBeTrue();
    expect(isValidProductCode('0012345678901')).toBeTrue();
  });

  it('rejeita vazio, caracteres não numéricos e mais de treze dígitos', () => {
    for (const value of ['', 'ABC', '12 3', '12345678901234']) {
      expect(isValidProductCode(value)).toBeFalse();
    }
  });
});
