export function maskIpv4Input(value: string): string {
  return value
    .replace(/[^\d.]/g, '')
    .split('.')
    .slice(0, 4)
    .map((octet) => octet.slice(0, 3))
    .join('.')
    .slice(0, 15);
}

export function normalizeIpv4(value: string): string | null {
  const octets = value.split('.');
  if (octets.length !== 4 || octets.some((octet) => !/^\d{1,3}$/.test(octet))) return null;
  const numbers = octets.map(Number);
  if (numbers.some((octet) => octet < 0 || octet > 255)) return null;
  return numbers.join('.');
}
