import { InjectionToken } from '@angular/core';
import type { BarcodeFormat } from '@capacitor-mlkit/barcode-scanning';

export type CameraPermission = 'prompt' | 'prompt-with-rationale' | 'granted' | 'denied' | 'limited';

export interface BarcodeScannerPort {
  checkPermissions(): Promise<{ camera: CameraPermission }>;
  requestPermissions(): Promise<{ camera: CameraPermission }>;
  scan(options: { formats: BarcodeFormat[] }): Promise<{
    barcodes: Array<{ rawValue?: string; displayValue?: string }>;
  }>;
  stopScan(): Promise<void>;
}

export const BARCODE_SCANNER = new InjectionToken<BarcodeScannerPort>('BARCODE_SCANNER');

