import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import {
  IonBackButton,
  IonButtons,
  IonHeader,
  IonTitle,
  IonToolbar
} from '@ionic/angular/standalone';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [IonBackButton, IonButtons, IonHeader, IonTitle, IonToolbar],
  templateUrl: './app-header.component.html',
  styleUrl: './app-header.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppHeaderComponent {
  readonly title = input<string | null>(null);
  readonly backHref = input<string | null>(null);
}
