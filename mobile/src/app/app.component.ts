import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import type { OnDestroy } from '@angular/core';
import { IonApp, IonRouterOutlet, IonSpinner } from '@ionic/angular/standalone';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [IonApp, IonRouterOutlet, IonSpinner],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppComponent implements OnDestroy {
  readonly showingSplash = signal(true);

  private readonly splashTimer = setTimeout(() => {
    this.showingSplash.set(false);
  }, 2000);

  ngOnDestroy(): void {
    clearTimeout(this.splashTimer);
  }
}

