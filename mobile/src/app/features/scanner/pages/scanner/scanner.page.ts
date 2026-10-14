import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import type { OnInit } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import {
  IonButton,
  IonContent,
  IonIcon,
  IonSpinner,
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { barcodeOutline, closeOutline } from 'ionicons/icons';
import { ConnectivityService } from '../../../../core/connectivity/connectivity.service';
import { NotificationService } from '../../../../core/feedback/notification.service';
import { AppHeaderComponent } from '../../../../shared/ui/app-header/app-header.component';
import { BottomNavigationComponent } from '../../../../shared/ui/bottom-navigation/bottom-navigation.component';
import { ConnectionStatusComponent } from '../../../../shared/ui/connection-status/connection-status.component';
import { PageStateComponent } from '../../../../shared/ui/page-state/page-state.component';
import { isValidProductCode } from '../../../../shared/utils/product-code.util';
import { ScannerFrameComponent } from '../../components/scanner-frame/scanner-frame.component';
import { BarcodeScannerService, ScannerPermissionDeniedError } from '../../data-access/barcode-scanner.service';

@Component({
  selector: 'app-scanner-page',
  standalone: true,
  imports: [ReactiveFormsModule, IonButton, IonContent, IonIcon, IonSpinner,
    AppHeaderComponent, BottomNavigationComponent,
    ConnectionStatusComponent, PageStateComponent, ScannerFrameComponent],
  templateUrl: './scanner.page.html',
  styleUrl: './scanner.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ScannerPage implements OnInit {
  readonly connectivity = inject(ConnectivityService);
  private readonly scanner = inject(BarcodeScannerService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly notifications = inject(NotificationService);

  readonly scanning = signal(false);
  readonly manualModalOpen = signal(false);
  readonly mode = this.route.snapshot.paramMap.get('modo') === 'alteracao' ? 'alteracao' : 'consulta';
  readonly manualCode = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(13), Validators.pattern(/^\d+$/)]
  });

  constructor() {
    addIcons({ barcodeOutline, closeOutline });
  }

  ngOnInit(): void {
    this.retryConnection();
  }

  retryConnection(): void {
    this.connectivity.check().subscribe();
  }

  async startScan(): Promise<void> {
    if (this.scanning() || this.connectivity.state() !== 'online') return;
    this.scanning.set(true);
    try {
      const code = await this.scanner.scan();
      if (code) {
        if (!isValidProductCode(code)) {
          await this.presentMessage('O código lido não é um EAN numérico válido.');
          return;
        }
        await this.navigateToProduct(code);
      }
    } catch (error: unknown) {
      const message = error instanceof ScannerPermissionDeniedError
        ? 'Permita o uso da câmera nas configurações do aparelho.'
        : 'Não foi possível iniciar o leitor. Tente novamente.';
      await this.presentMessage(message);
    } finally {
      this.scanning.set(false);
    }
  }

  enterManually(): void {
    this.manualCode.reset();
    this.manualModalOpen.set(true);
  }

  closeManualModal(): void {
    this.manualModalOpen.set(false);
  }

  searchManualCode(): void {
    const code = this.manualCode.value.trim();
    if (this.manualCode.invalid || !isValidProductCode(code)) {
      this.manualCode.markAsTouched();
      return;
    }
    this.manualModalOpen.set(false);
    void this.navigateToProduct(code);
  }

  private async presentMessage(message: string): Promise<void> {
    await this.notifications.show(message, { color: 'danger' });
  }

  private navigateToProduct(code: string): Promise<boolean> {
    const destination = this.mode === 'alteracao' ? '/produto' : '/consulta-estoque';
    return this.router.navigate([destination, code]);
  }
}
