import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { BrlCurrencyPipe } from '../../../../shared/pipes/brl-currency.pipe';
import { BarcodeSymbolComponent } from '../../../../shared/ui/barcode-symbol/barcode-symbol.component';

@Component({
  selector: 'app-price-change-summary',
  standalone: true,
  imports: [BarcodeSymbolComponent, BrlCurrencyPipe],
  templateUrl: './price-change-summary.component.html',
  styleUrl: './price-change-summary.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PriceChangeSummaryComponent {
  readonly description = input.required<string>();
  readonly barcode = input.required<string>();
  readonly previousPrice = input.required<number>();
  readonly newPrice = input.required<number>();
}
