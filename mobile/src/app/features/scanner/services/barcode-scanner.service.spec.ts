import { TestBed } from '@angular/core/testing';
import { BARCODE_SCANNER, type BarcodeScannerPort } from './barcode-scanner.port';
import { BarcodeScannerService, ScannerPermissionDeniedError } from './barcode-scanner.service';

describe('BarcodeScannerService', () => {
  let scanner: jasmine.SpyObj<BarcodeScannerPort>;
  let service: BarcodeScannerService;

  beforeEach(() => {
    scanner = jasmine.createSpyObj<BarcodeScannerPort>('scanner',
      ['checkPermissions', 'requestPermissions', 'scan', 'stopScan']);
    scanner.stopScan.and.resolveTo();
    TestBed.configureTestingModule({ providers: [{ provide: BARCODE_SCANNER, useValue: scanner }] });
    service = TestBed.inject(BarcodeScannerService);
  });

  it('lê conteúdo com permissão já concedida e finaliza', async () => {
    scanner.checkPermissions.and.resolveTo({ camera: 'granted' });
    scanner.scan.and.resolveTo({ barcodes: [{ rawValue: '00123' }] });
    await expectAsync(service.scan()).toBeResolvedTo('00123');
    expect(scanner.requestPermissions).not.toHaveBeenCalled();
    expect(scanner.stopScan).toHaveBeenCalled();
  });

  it('solicita permissão quando necessário', async () => {
    scanner.checkPermissions.and.resolveTo({ camera: 'prompt' });
    scanner.requestPermissions.and.resolveTo({ camera: 'granted' });
    scanner.scan.and.resolveTo({ barcodes: [{ displayValue: '789' }] });
    await expectAsync(service.scan()).toBeResolvedTo('789');
  });

  it('rejeita permissão negada sem abrir scanner', async () => {
    scanner.checkPermissions.and.resolveTo({ camera: 'denied' });
    scanner.requestPermissions.and.resolveTo({ camera: 'denied' });
    await expectAsync(service.scan()).toBeRejectedWithError(ScannerPermissionDeniedError);
    expect(scanner.scan).not.toHaveBeenCalled();
  });

  it('retorna nulo no cancelamento e finaliza também quando há erro', async () => {
    scanner.checkPermissions.and.resolveTo({ camera: 'granted' });
    scanner.scan.and.resolveTo({ barcodes: [] });
    await expectAsync(service.scan()).toBeResolvedTo(null);

    scanner.scan.and.rejectWith(new Error('camera'));
    await expectAsync(service.scan()).toBeRejected();
    expect(scanner.stopScan).toHaveBeenCalledTimes(2);
  });

  it('permite interromper explicitamente', async () => {
    await service.stop();
    expect(scanner.stopScan).toHaveBeenCalled();
  });
});
