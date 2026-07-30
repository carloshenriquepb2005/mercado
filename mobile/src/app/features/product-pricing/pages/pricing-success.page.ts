import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { IonContent, IonHeader, IonTitle, IonToolbar } from '@ionic/angular/standalone';
import { BottomNavigationComponent } from '../../../shared/components/bottom-navigation/bottom-navigation.component';
import { PricingSuccessService } from '../services/pricing-success.service';

@Component({
  selector: 'app-pricing-success-page',
  standalone: true,
  imports: [IonContent, IonHeader, IonTitle, IonToolbar, BottomNavigationComponent],
  templateUrl: './pricing-success.page.html',
  styleUrl: './pricing-success.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PricingSuccessPage {
  readonly success = inject(PricingSuccessService);
  private readonly router = inject(Router);

  scanAnother(): void {
    this.success.clear();
    void this.router.navigateByUrl('/scanner');
  }

  goHome(): void {
    this.success.clear();
    void this.router.navigateByUrl('/home');
  }

  formatPrice(value: number): string {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value);
  }
}
