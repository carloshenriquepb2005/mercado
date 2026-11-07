import type { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export const MAX_STOCK_QUANTITY = 9_999_999.999;

/** Quantidade > 0, com até sete dígitos inteiros e três casas decimais (`,` ou `.`). */
export const quantityValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const raw = String(control.value ?? '').trim();
  if (!raw) return null;
  if (!/^\d{1,7}([.,]\d{1,3})?$/.test(raw)) return { quantity: true };
  const value = Number(raw.replace(',', '.'));
  return Number.isFinite(value) && value > 0 && value <= MAX_STOCK_QUANTITY ? null : { quantity: true };
};
