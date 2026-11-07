/**
 * Converte o texto digitado em um preço com duas casas fixas: mantém só dígitos,
 * descarta zeros à esquerda, limita a dez dígitos (oito inteiros + centavos)
 * e desloca a vírgula automaticamente. Retorna '' quando não há dígitos.
 */
export const maskPriceDigits = (rawValue: string | null | undefined): string => {
  const digits = String(rawValue ?? '')
    .replace(/\D/g, '')
    .replace(/^0+/, '')
    .slice(0, 10);
  if (!digits) return '';

  const paddedDigits = digits.padStart(3, '0');
  return `${paddedDigits.slice(0, -2)},${paddedDigits.slice(-2)}`;
};

/** Converte texto numérico em formato brasileiro ou com ponto (ex.: '10,50') para número. */
export const parseDecimal = (rawValue: string | null | undefined): number =>
  Number(String(rawValue ?? '').trim().replace(',', '.'));
