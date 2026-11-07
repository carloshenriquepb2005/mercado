import { maskPriceDigits, parseDecimal } from './price-mask.util';

describe('price-mask.util', () => {
  describe('maskPriceDigits', () => {
    it('desloca a vírgula mantendo duas casas', () => {
      expect(maskPriceDigits('1')).toBe('0,01');
      expect(maskPriceDigits('0,012')).toBe('0,12');
      expect(maskPriceDigits('0,123')).toBe('1,23');
      expect(maskPriceDigits('1,234')).toBe('12,34');
    });

    it('ignora caracteres não numéricos e limita a dez dígitos', () => {
      expect(maskPriceDigits('R$ 12,34')).toBe('12,34');
      expect(maskPriceDigits('123456789012')).toBe('12345678,90');
    });

    it('retorna vazio quando não há dígitos significativos', () => {
      expect(maskPriceDigits('')).toBe('');
      expect(maskPriceDigits(null)).toBe('');
      expect(maskPriceDigits(undefined)).toBe('');
      expect(maskPriceDigits('000')).toBe('');
      expect(maskPriceDigits('abc')).toBe('');
    });
  });

  describe('parseDecimal', () => {
    it('aceita vírgula ou ponto', () => {
      expect(parseDecimal('10,50')).toBe(10.5);
      expect(parseDecimal('12.345')).toBe(12.345);
      expect(parseDecimal(' 3 ')).toBe(3);
    });

    it('retorna NaN para texto inválido ou vazio sem valor', () => {
      expect(parseDecimal('abc')).toBeNaN();
      expect(parseDecimal(null)).toBe(0);
    });
  });
});
