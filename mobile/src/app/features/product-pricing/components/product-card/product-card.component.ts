import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { BrlCurrencyPipe } from '../../../../shared/pipes/brl-currency.pipe';
import { BarcodeSymbolComponent } from '../../../../shared/ui/barcode-symbol/barcode-symbol.component';
import type { ProdutoResponse } from '../../models/produto-response';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [BarcodeSymbolComponent, BrlCurrencyPipe],
  templateUrl: './product-card.component.html',
  styleUrl: './product-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductCardComponent {
  readonly product = input.required<ProdutoResponse>();
}
