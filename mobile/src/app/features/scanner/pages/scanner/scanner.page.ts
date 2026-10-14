import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import type { OnInit } from '@angular/core';
import { Router } from '@angular/router';
import {
  AlertController,
  IonButton,
  IonContent,
  IonSpinner,
} from '@ionic/angular/standalone';
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
  imports: [IonButton, IonContent, IonSpinner, AppHeaderComponent, BottomNavigationComponent,
    ConnectionStatusComponent, PageStateComponent, ScannerFrameComponent],
  templateUrl: './scanner.page.html',
  styleUrl: './scanner.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ScannerPage implements OnInit {
  readonly connectivity = inject(ConnectivityService);
  private readonly scanner = inject(BarcodeScannerService);
  private readonly router = inject(Router);
  private readonly alerts = inject(AlertController);
  private readonly notifications = inject(NotificationService);

  readonly scanning = signal(false);

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
        await this.router.navigate(['/produto', code]);
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

  async enterManually(): Promise<void> {
    const alert = await this.alerts.create({
      header: 'Digitar código',
      message: 'Informe até 13 dígitos, preservando zeros à esquerda.',
      inputs: [{ name: 'codigo', type: 'number', placeholder: 'Código de barras', attributes: { inputmode: 'numeric' } }],
      buttons: [
        { text: 'Cancelar', role: 'cancel' },
        {
          text: 'Buscar',
          handler: (data) => {
            const code = String(data.codigo ?? '').trim();
            if (!isValidProductCode(code)) return false;
            void this.router.navigate(['/produto', code]);
            return true;
          }
        }
      ]
    });
    await alert.present();
  }

  private async presentMessage(message: string): Promise<void> {
    await this.notifications.show(message, { color: 'danger' });
  }
}
