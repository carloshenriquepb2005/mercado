import { inject, Injectable } from '@angular/core';
import { ToastController } from '@ionic/angular/standalone';

export interface NotificationOptions {
  color?: 'danger' | 'primary' | 'success';
  duration?: number;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly toasts = inject(ToastController);

  async show(message: string, options: NotificationOptions = {}): Promise<void> {
    const toast = await this.toasts.create({
      message,
      duration: options.duration ?? 3000,
      position: 'bottom',
      color: options.color ?? 'primary'
    });
    await toast.present();
  }
}
