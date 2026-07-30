import type { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export const priceValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const raw = String(control.value ?? '').trim();
  if (!raw) return null;
  if (!/^\d{1,8}([.,]\d{1,2})?$/.test(raw)) return { price: true };
  const value = Number(raw.replace(',', '.'));
  return Number.isFinite(value) && value > 0 && value <= 99_999_999.99 ? null : { price: true };
};
