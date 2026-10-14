import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ApiConfigService } from '../config/api-config.service';
import { ConnectivityService } from './connectivity.service';

describe('ConnectivityService', () => {
  let service: ConnectivityService;
  let http: HttpTestingController;
  const config = jasmine.createSpyObj<ApiConfigService>('config', ['baseUrl']);

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(),
        { provide: ApiConfigService, useValue: config }]
    });
    config.baseUrl.and.returnValue('http://servidor:8080');
    service = TestBed.inject(ConnectivityService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('expõe online quando o health está UP', () => {
    service.check().subscribe((available) => expect(available).toBeTrue());
    expect(service.state()).toBe('checking');
    http.expectOne('http://servidor:8080/actuator/health').flush({ status: 'UP' });
    expect(service.state()).toBe('online');
  });

  it('expõe offline em erro ou health diferente de UP', () => {
    service.check().subscribe((available) => expect(available).toBeFalse());
    http.expectOne('http://servidor:8080/actuator/health')
      .flush(null, { status: 503, statusText: 'Unavailable' });
    expect(service.state()).toBe('offline');

    service.check().subscribe((available) => expect(available).toBeFalse());
    http.expectOne('http://servidor:8080/actuator/health').flush({ status: 'DOWN' });
    expect(service.state()).toBe('offline');
  });

  it('expõe offline sem configuração salva', () => {
    config.baseUrl.and.returnValue('');
    service.check().subscribe((available) => expect(available).toBeFalse());
    expect(service.state()).toBe('offline');
  });
});

