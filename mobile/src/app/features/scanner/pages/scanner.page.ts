import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import type { OnInit } from '@angular/core';
import { Router } from '@angular/router';
import {
  AlertController,
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonSpinner,
  IonTitle,
  IonToolbar,
  ToastController
} from '@ionic/angular/standalone';
import { ConnectivityService } from '../../../core/services/connectivity.service';
import { BarcodeScannerService, ScannerPermissionDeniedError } from '../services/barcode-scanner.service';

@Component({
  selector: 'app-scanner-page',
  standalone: true,
  imports: [IonButton, IonButtons, IonContent, IonHeader, IonSpinner, IonTitle, IonToolbar],
  templateUrl: './scanner.page.html',
  styleUrl: './scanner.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ScannerPage implements OnInit {
  readonly connectivity = inject(ConnectivityService);
  private readonly scanner = inject(BarcodeScannerService);
  private readonly router = inject(Router);
  private readonly alerts = inject(AlertController);
  private readonly toasts = inject(ToastController);

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
        if (!/^\d{1,13}$/.test(code)) {
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
            if (!/^\d{1,13}$/.test(code)) return false;
            void this.router.navigate(['/produto', code]);
            return true;
          }
        }
      ]
    });
    await alert.present();
  }

  private async presentMessage(message: string): Promise<void> {
    const toast = await this.toasts.create({ message, duration: 3000, position: 'bottom', color: 'danger' });
    await toast.present();
  }
}
