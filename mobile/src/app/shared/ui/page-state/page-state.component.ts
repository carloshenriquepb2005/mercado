import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { IonButton, IonSpinner } from '@ionic/angular/standalone';

@Component({
  selector: 'app-page-state',
  standalone: true,
  imports: [IonButton, IonSpinner],
  templateUrl: './page-state.component.html',
  styleUrl: './page-state.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PageStateComponent {
  readonly kind = input<'loading' | 'error'>('loading');
  readonly title = input('');
  readonly description = input('');
  readonly info = input('');
  readonly actionLabel = input('');
  readonly action = output<void>();
}
