import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import type { OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { IonContent, IonIcon } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { barcodeOutline, calculatorOutline, cubeOutline, logOutOutline, pricetagOutline, settingsOutline } from 'ionicons/icons';
import { AuthSessionService } from '../../../../core/auth/auth-session.service';
import { ConnectivityService } from '../../../../core/connectivity/connectivity.service';
import { NotificationService } from '../../../../core/feedback/notification.service';
import { AppHeaderComponent } from '../../../../shared/ui/app-header/app-header.component';
import { BottomNavigationComponent } from '../../../../shared/ui/bottom-navigation/bottom-navigation.component';
import { ConnectionStatusComponent } from '../../../../shared/ui/connection-status/connection-status.component';
import { HomeActionCardComponent } from '../../components/home-action-card/home-action-card.component';

@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [IonContent, IonIcon, AppHeaderComponent, BottomNavigationComponent,
    ConnectionStatusComponent, HomeActionCardComponent],
  templateUrl: './home.page.html',
  styleUrl: './home.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HomePage implements OnInit {
  readonly connectivity = inject(ConnectivityService);
  readonly session = inject(AuthSessionService);
  private readonly router = inject(Router);
  private readonly notifications = inject(NotificationService);

  constructor() {
    addIcons({ barcodeOutline, calculatorOutline, cubeOutline, logOutOutline, pricetagOutline, settingsOutline });
  }

  ngOnInit(): void {
    this.connectivity.check().subscribe();
  }

  logout(): void {
    this.session.end();
    void this.router.navigateByUrl('/login', { replaceUrl: true });
  }

  async comingSoon(feature: string): Promise<void> {
    await this.notifications.show(
      `${feature} estará disponível em uma próxima versão.`,
      { duration: 2600 }
    );
  }
}
