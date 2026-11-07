import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { IonContent } from '@ionic/angular/standalone';
import { BrlCurrencyPipe } from '../../../../shared/pipes/brl-currency.pipe';
import { AppHeaderComponent } from '../../../../shared/ui/app-header/app-header.component';
import { BarcodeSymbolComponent } from '../../../../shared/ui/barcode-symbol/barcode-symbol.component';
import { BottomNavigationComponent } from '../../../../shared/ui/bottom-navigation/bottom-navigation.component';
import { StockEntrySuccessStore } from '../../state/stock-entry-success.store';

@Component({
  selector: 'app-stock-entry-success-page',
  standalone: true,
  imports: [IonContent, AppHeaderComponent, BarcodeSymbolComponent, BottomNavigationComponent, BrlCurrencyPipe],
  templateUrl: './stock-entry-success.page.html',
  styleUrl: './stock-entry-success.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class StockEntrySuccessPage {
  readonly success = inject(StockEntrySuccessStore);
  private readonly router = inject(Router);

  newEntry(): void {
    this.success.clear();
    void this.router.navigateByUrl('/scanner/entrada');
  }

  goHome(): void {
    this.success.clear();
    void this.router.navigateByUrl('/home');
  }
}
