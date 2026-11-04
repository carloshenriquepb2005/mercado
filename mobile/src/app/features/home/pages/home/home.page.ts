import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import type { OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { IonContent, IonIcon, Platform } from '@ionic/angular/standalone';
import type { ViewDidEnter, ViewWillLeave } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { barcodeOutline, calculatorOutline, cubeOutline, logOut, pricetagOutline, settingsOutline } from 'ionicons/icons';
import type { Subscription } from 'rxjs';
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
export class HomePage implements OnInit, ViewDidEnter, ViewWillLeave {
  readonly connectivity = inject(ConnectivityService);
  readonly session = inject(AuthSessionService);
  readonly logoutModalOpen = signal(false);
  private readonly router = inject(Router);
  private readonly notifications = inject(NotificationService);
  private readonly platform = inject(Platform);
  private backButtonSubscription: Subscription | null = null;

  constructor() {
    addIcons({ barcodeOutline, calculatorOutline, cubeOutline, logOut, pricetagOutline, settingsOutline });
  }

  ngOnInit(): void {
    this.connectivity.check().subscribe();
  }

  ionViewDidEnter(): void {
    this.backButtonSubscription?.unsubscribe();
    this.backButtonSubscription = this.platform.backButton.subscribeWithPriority(10, () => {
      if (this.logoutModalOpen()) {
        this.cancelLogout();
        return;
      }
      this.requestLogout();
    });
  }

  ionViewWillLeave(): void {
    this.backButtonSubscription?.unsubscribe();
    this.backButtonSubscription = null;
    this.cancelLogout();
  }

  requestLogout(): void {
    this.logoutModalOpen.set(true);
  }

  cancelLogout(): void {
    this.logoutModalOpen.set(false);
  }

  confirmLogout(): void {
    this.cancelLogout();
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
