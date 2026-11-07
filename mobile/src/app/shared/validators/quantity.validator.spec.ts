import { FormControl } from '@angular/forms';
import { quantityValidator } from './quantity.validator';

describe('quantityValidator', () => {
  const validate = (value: string | null) => quantityValidator(new FormControl(value));

  it('aceita vazio (a obrigatoriedade é de Validators.required)', () => {
    expect(validate('')).toBeNull();
    expect(validate(null)).toBeNull();
  });

  it('aceita quantidades válidas com vírgula ou ponto e até três casas', () => {
    for (const value of ['1', '12,5', '12.5', '0,001', '0.5', '9999999,999', '9999999']) {
      expect(validate(value)).withContext(value).toBeNull();
    }
  });

  it('rejeita zero, negativos, texto e formatos inválidos', () => {
    for (const value of ['0', '0,000', '-1', 'abc', '1,2345', '1,', ',5', '1,2,3', '12 3', '1e3']) {
      expect(validate(value)).withContext(value).toEqual({ quantity: true });
    }
  });

  it('rejeita valores acima do limite de sete dígitos inteiros', () => {
    expect(validate('10000000')).toEqual({ quantity: true });
    expect(validate('9999999,9991')).toEqual({ quantity: true });
  });
});
