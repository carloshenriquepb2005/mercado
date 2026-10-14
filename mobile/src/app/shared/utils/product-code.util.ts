export const isValidProductCode = (value: string): boolean => /^\d{1,13}$/.test(value);
