import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-barcode-symbol',
  standalone: true,
  templateUrl: './barcode-symbol.component.html',
  styleUrl: './barcode-symbol.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BarcodeSymbolComponent {
  readonly size = input<'small' | 'medium' | 'large'>('medium');
  readonly variant = input<'brand' | 'neutral'>('neutral');
}
