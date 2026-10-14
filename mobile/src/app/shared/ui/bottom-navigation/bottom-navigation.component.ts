import { ChangeDetectionStrategy, Component, inject, input } from '@angular/core';
import { Router } from '@angular/router';
import { IonIcon } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { barcodeOutline, home, pricetagOutline, settingsOutline } from 'ionicons/icons';

@Component({
  selector: 'app-bottom-navigation',
  standalone: true,
  imports: [IonIcon],
  templateUrl: './bottom-navigation.component.html',
  styleUrl: './bottom-navigation.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BottomNavigationComponent {
  private readonly router = inject(Router);
  readonly active = input<'home' | 'scanner' | 'settings'>('home');

  constructor() {
    addIcons({ home, barcodeOutline, pricetagOutline, settingsOutline });
  }

  goTo(url: '/home' | '/scanner/consulta' | '/ajustes'): void {
    void this.router.navigateByUrl(url);
  }
}
