import { FormControl } from '@angular/forms';
import { optionalIsoDateValidator } from './iso-date.validator';

describe('optionalIsoDateValidator', () => {
  const validate = (value: string | null) => optionalIsoDateValidator(new FormControl(value));

  it('aceita vazio como data não informada', () => {
    expect(validate('')).toBeNull();
    expect(validate(null)).toBeNull();
    expect(validate('   ')).toBeNull();
  });

  it('aceita datas reais, incluindo 29/02 de ano bissexto', () => {
    expect(validate('2027-03-15')).toBeNull();
    expect(validate('2028-02-29')).toBeNull();
  });

  it('rejeita formato incorreto e datas inexistentes', () => {
    for (const value of ['15/03/2027', '2027-3-15', '2027-02-30', '2027-13-01', '2027-00-10', '1800-01-01', 'abc']) {
      expect(validate(value)).withContext(value).toEqual({ isoDate: true });
    }
  });
});
