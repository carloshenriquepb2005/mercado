import { inject, Injectable } from '@angular/core';
import { BarcodeFormat } from '@capacitor-mlkit/barcode-scanning';
import { BARCODE_SCANNER } from './barcode-scanner.port';

export class ScannerPermissionDeniedError extends Error {
  constructor() {
    super('Permissão de câmera não concedida.');
    this.name = 'ScannerPermissionDeniedError';
  }
}

@Injectable({ providedIn: 'root' })
export class BarcodeScannerService {
  private readonly scanner = inject(BARCODE_SCANNER);

  async scan(): Promise<string | null> {
    const currentPermission = await this.scanner.checkPermissions();
    const cameraPermission = currentPermission.camera === 'granted'
      ? currentPermission.camera
      : (await this.scanner.requestPermissions()).camera;

    if (cameraPermission !== 'granted') {
      throw new ScannerPermissionDeniedError();
    }

    try {
      const result = await this.scanner.scan({
        formats: [BarcodeFormat.Ean8, BarcodeFormat.Ean13, BarcodeFormat.UpcA,
          BarcodeFormat.UpcE, BarcodeFormat.Code128]
      });
      const barcode = result.barcodes[0];
      return (barcode?.rawValue ?? barcode?.displayValue ?? '').trim() || null;
    } finally {
      await this.scanner.stopScan().catch(() => undefined);
    }
  }

  async stop(): Promise<void> {
    await this.scanner.stopScan();
  }
}
