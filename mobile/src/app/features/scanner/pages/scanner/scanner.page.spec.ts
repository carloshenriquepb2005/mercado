import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { provideIonicAngular } from '@ionic/angular/standalone';
import { of } from 'rxjs';
import { ConnectivityService } from '../../../../core/connectivity/connectivity.service';
import { NotificationService } from '../../../../core/feedback/notification.service';
import { BarcodeScannerService, ScannerPermissionDeniedError } from '../../data-access/barcode-scanner.service';
import { ScannerPage } from './scanner.page';

describe('ScannerPage', () => {
  let scannerMode = 'consulta';
  const connection = { state: signal<'checking' | 'online' | 'offline'>('online'), check: jasmine.createSpy().and.returnValue(of(true)) };
  const scanner = jasmine.createSpyObj<BarcodeScannerService>('scanner', ['scan']);
  const router = jasmine.createSpyObj<Router>('router', ['navigate']);
  const notifications = jasmine.createSpyObj<NotificationService>('notifications', ['show']);

  beforeEach(() => {
    router.navigate.and.resolveTo(true);
    TestBed.configureTestingModule({
      imports: [ScannerPage],
      providers: [provideIonicAngular(),
        { provide: ConnectivityService, useValue: connection },
        { provide: BarcodeScannerService, useValue: scanner },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => scannerMode } } } },
        { provide: Router, useValue: router },
        { provide: NotificationService, useValue: notifications }]
    });
  });

  afterEach(() => {
    scanner.scan.calls.reset();
    router.navigate.calls.reset();
    notifications.show.calls.reset();
    connection.state.set('online');
    scannerMode = 'consulta';
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
    expect(router.navigate).toHaveBeenCalledWith(['/consulta-estoque', '00123']);
  });

  it('encaminha para edição quando o modo é alteração', async () => {
    scannerMode = 'alteracao';
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
    expect(notifications.show).toHaveBeenCalled();
  });

  it('informa código lido inválido e restaura o estado', async () => {
    scanner.scan.and.resolveTo('ABC');
    const page = TestBed.createComponent(ScannerPage).componentInstance;
    await page.startScan();
    expect(router.navigate).not.toHaveBeenCalled();
    expect(page.scanning()).toBeFalse();
    expect(notifications.show).toHaveBeenCalled();
  });

  it('abre a entrada manual no padrão de modal do aplicativo', () => {
    const fixture = TestBed.createComponent(ScannerPage);
    fixture.componentInstance.enterManually();
    fixture.detectChanges();
    expect(fixture.componentInstance.manualModalOpen()).toBeTrue();
    expect(fixture.nativeElement.querySelector('.manual-modal')).not.toBeNull();
  });

  it('valida e navega com o código digitado preservando zeros à esquerda', () => {
    const page = TestBed.createComponent(ScannerPage).componentInstance;
    page.enterManually();
    page.manualCode.setValue('00123');
    page.searchManualCode();
    expect(router.navigate).toHaveBeenCalledWith(['/consulta-estoque', '00123']);
    expect(page.manualModalOpen()).toBeFalse();
  });

  it('mantém o modal aberto quando o código manual é inválido', () => {
    const page = TestBed.createComponent(ScannerPage).componentInstance;
    page.enterManually();
    page.manualCode.setValue('ABC');
    page.searchManualCode();
    expect(router.navigate).not.toHaveBeenCalled();
    expect(page.manualCode.touched).toBeTrue();
    expect(page.manualModalOpen()).toBeTrue();
  });
});
