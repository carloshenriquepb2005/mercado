import { maskIpv4Input, normalizeIpv4 } from './ip-address.util';

describe('IPv4 utils', () => {
  it('remove caracteres inválidos e limita octetos e quantidade de grupos', () => {
    expect(maskIpv4Input('192a.1689.0.10.77')).toBe('192.168.0.10');
  });

  it('valida e normaliza os quatro octetos', () => {
    expect(normalizeIpv4('192.168.001.010')).toBe('192.168.1.10');
    expect(normalizeIpv4('192.168.0')).toBeNull();
    expect(normalizeIpv4('192.168.0.256')).toBeNull();
    expect(normalizeIpv4('192.168.x.1')).toBeNull();
  });
});
