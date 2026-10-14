import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import type { ApiConnectionState } from '../../../core/connectivity/connectivity.service';

@Component({
  selector: 'app-connection-status',
  standalone: true,
  templateUrl: './connection-status.component.html',
  styleUrl: './connection-status.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ConnectionStatusComponent {
  readonly state = input.required<ApiConnectionState>();
  readonly compact = input(false);
  readonly interactive = input(false);
  readonly retry = output<void>();
  readonly label = computed(() => {
    if (this.state() === 'checking') return this.compact() ? 'Verificando' : 'Verificando servidor';
    if (this.state() === 'online') return this.compact() ? 'Online' : 'Servidor Online';
    return this.compact() ? 'Offline' : 'Servidor Offline';
  });
}
