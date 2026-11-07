import type { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/** Data opcional no formato yyyy-MM-dd; vazio é válido e significa "não informada". */
export const optionalIsoDateValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const raw = String(control.value ?? '').trim();
  if (!raw) return null;
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(raw);
  if (!match) return { isoDate: true };

  const [year, month, day] = [Number(match[1]), Number(match[2]), Number(match[3])];
  const date = new Date(Date.UTC(year, month - 1, day));
  const valid = date.getUTCFullYear() === year && date.getUTCMonth() === month - 1 && date.getUTCDate() === day;
  return valid && year >= 1900 ? null : { isoDate: true };
};
