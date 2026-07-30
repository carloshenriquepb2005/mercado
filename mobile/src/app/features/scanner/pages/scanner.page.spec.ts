import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AlertController, provideIonicAngular, ToastController } from '@ionic/angular/standalone';
import { of } from 'rxjs';
import { ConnectivityService } from '../../../core/services/connectivity.service';
import { BarcodeScannerService, ScannerPermissionDeniedError } from '../services/barcode-scanner.service';
import { ScannerPage } from './scanner.page';

describe('ScannerPage', () => {
  const connection = { state: signal<'checking' | 'online' | 'offline'>('online'), check: jasmine.createSpy().and.returnValue(of(true)) };
  const scanner = jasmine.createSpyObj<BarcodeScannerService>('scanner', ['scan']);
  const router = jasmine.createSpyObj<Router>('router', ['navigate']);
  const present = jasmine.createSpy().and.resolveTo();
  const toasts = { create: jasmine.createSpy().and.resolveTo({ present }) };
  const alerts = { create: jasmine.createSpy().and.resolveTo({ present }) };

  beforeEach(() => {
    router.navigate.and.resolveTo(true);
    TestBed.configureTestingModule({
      imports: [ScannerPage],
      providers: [provideIonicAngular(),
        { provide: ConnectivityService, useValue: connection },
        { provide: BarcodeScannerService, useValue: scanner },
        { provide: Router, useValue: router },
        { provide: ToastController, useValue: toasts },
        { provide: AlertController, useValue: alerts }]
    });
  });

  afterEach(() => {
    scanner.scan.calls.reset();
    router.navigate.calls.reset();
    connection.state.set('online');
  });

  it('verifica conexão ao iniciar', () => {
    const fixture = TestBed.createComponent(ScannerPage);
    fixture.detectChanges();
    expect(connection.check).toHaveBeenCalled();
  });

  it('navega com o código lido', async () => {
    scanner.scan.and.resolveTo('00123');
    const page = TestBed.createComponent(ScannerPage).componentInstance;
    await page.startScan();
    expect(router.navigate).toHaveBeenCalledWith(['/produto', '00123']);
  });

  it('não abre scanner quando servidor está offline', async () => {
    connection.state.set('offline');
    const page = TestBed.createComponent(ScannerPage).componentInstance;
    await page.startScan();
    expect(scanner.scan).not.toHaveBeenCalled();
  });

  it('trata permissão negada sem navegar', async () => {
    scanner.scan.and.rejectWith(new ScannerPermissionDeniedError());
    const page = TestBed.createComponent(ScannerPage).componentInstance;
    await page.startScan();
    expect(router.navigate).not.toHaveBeenCalled();
    expect(toasts.create).toHaveBeenCalled();
  });

  it('informa código lido inválido e restaura o estado', async () => {
    scanner.scan.and.resolveTo('ABC');
    const page = TestBed.createComponent(ScannerPage).componentInstance;
    await page.startScan();
    expect(router.navigate).not.toHaveBeenCalled();
    expect(page.scanning()).toBeFalse();
    expect(toasts.create).toHaveBeenCalled();
  });

  it('abre a entrada manual', async () => {
    const page = TestBed.createComponent(ScannerPage).componentInstance;
    await page.enterManually();
    expect(alerts.create).toHaveBeenCalled();
    expect(present).toHaveBeenCalled();
  });
});
