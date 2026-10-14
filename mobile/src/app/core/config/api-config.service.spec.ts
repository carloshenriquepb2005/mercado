import { TestBed } from '@angular/core/testing';
import { ApiConfigService } from './api-config.service';

describe('ApiConfigService', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
  });

  afterEach(() => localStorage.clear());

  it('inicia sem URL e salva o IP com a porta da API', () => {
    const service = TestBed.inject(ApiConfigService);
    expect(service.hasSavedConfiguration()).toBeFalse();
    expect(service.baseUrl()).toBe('');

    service.save('192.168.0.10');
    expect(service.hasSavedConfiguration()).toBeTrue();
    expect(service.baseUrl()).toBe('http://192.168.0.10:8080');
    expect(service.currentIp()).toBe('192.168.0.10');
  });

  it('restaura uma configuração persistida e ignora valor inválido', () => {
    localStorage.setItem('precificacao.config.apiUrl', 'http://10.0.0.8:8080/');
    const restored = new ApiConfigService();
    expect(restored.baseUrl()).toBe('http://10.0.0.8:8080');

    localStorage.setItem('precificacao.config.apiUrl', 'javascript:alert(1)');
    expect(new ApiConfigService().hasSavedConfiguration()).toBeFalse();

    localStorage.setItem('precificacao.config.apiUrl', 'http://servidor-invalido:8080');
    expect(new ApiConfigService().hasSavedConfiguration()).toBeFalse();
  });
});
