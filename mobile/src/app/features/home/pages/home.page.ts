import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import type { OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { IonContent, IonHeader, IonIcon, IonTitle, IonToolbar, ToastController } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { barcodeOutline, calculatorOutline, logOutOutline, pricetagOutline, settingsOutline } from 'ionicons/icons';
import { AuthSessionService } from '../../../core/auth/auth-session.service';
import { ConnectivityService } from '../../../core/services/connectivity.service';
import { BottomNavigationComponent } from '../../../shared/components/bottom-navigation/bottom-navigation.component';

@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [RouterLink, IonContent, IonHeader, IonIcon, IonTitle, IonToolbar, BottomNavigationComponent],
  templateUrl: './home.page.html',
  styleUrl: './home.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HomePage implements OnInit {
  readonly connectivity = inject(ConnectivityService);
  readonly session = inject(AuthSessionService);
  private readonly router = inject(Router);
  private readonly toasts = inject(ToastController);

  constructor() {
    addIcons({ barcodeOutline, calculatorOutline, logOutOutline, pricetagOutline, settingsOutline });
  }

  ngOnInit(): void {
    this.connectivity.check().subscribe();
  }

  logout(): void {
    this.session.end();
    void this.router.navigateByUrl('/login', { replaceUrl: true });
  }

  async comingSoon(feature: string): Promise<void> {
    const toast = await this.toasts.create({
      message: `${feature} estará disponível em uma próxima versão.`,
      duration: 2600,
      position: 'bottom',
      color: 'primary'
    });
    await toast.present();
  }
}
