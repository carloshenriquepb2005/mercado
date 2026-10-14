import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { IonContent } from '@ionic/angular/standalone';
import { AppHeaderComponent } from '../../../../shared/ui/app-header/app-header.component';
import { BottomNavigationComponent } from '../../../../shared/ui/bottom-navigation/bottom-navigation.component';
import { PriceChangeSummaryComponent } from '../../components/price-change-summary/price-change-summary.component';
import { PricingSuccessStore } from '../../state/pricing-success.store';

@Component({
  selector: 'app-pricing-success-page',
  standalone: true,
  imports: [IonContent, AppHeaderComponent, BottomNavigationComponent, PriceChangeSummaryComponent],
  templateUrl: './pricing-success.page.html',
  styleUrl: './pricing-success.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PricingSuccessPage {
  readonly success = inject(PricingSuccessStore);
  private readonly router = inject(Router);

  scanAnother(): void {
    this.success.clear();
    void this.router.navigateByUrl('/scanner/alteracao');
  }

  goHome(): void {
    this.success.clear();
    void this.router.navigateByUrl('/home');
  }
}
