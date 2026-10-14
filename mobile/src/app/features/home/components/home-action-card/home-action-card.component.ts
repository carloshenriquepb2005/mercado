import { NgTemplateOutlet } from '@angular/common';
import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { IonIcon } from '@ionic/angular/standalone';

@Component({
  selector: 'app-home-action-card',
  standalone: true,
  imports: [IonIcon, NgTemplateOutlet, RouterLink],
  templateUrl: './home-action-card.component.html',
  styleUrl: './home-action-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HomeActionCardComponent {
  readonly title = input.required<string>();
  readonly description = input('');
  readonly icon = input.required<string>();
  readonly link = input<string | null>(null);
  readonly badge = input('');
  readonly disabled = input(false);
  readonly variant = input<'featured' | 'compact' | 'wide'>('compact');
  readonly tone = input<'primary' | 'orange' | 'muted'>('primary');
  readonly selected = output<void>();
}
