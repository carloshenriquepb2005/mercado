import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import type { OnDestroy, OnInit } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { IonContent, IonIcon, IonSpinner } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import {
  checkmarkCircle, closeCircle, closeCircleOutline, cloudOfflineOutline,
  informationCircleOutline, lockClosedOutline, lockOpenOutline,
  saveOutline, serverOutline, wifiOutline
} from 'ionicons/icons';
import { finalize } from 'rxjs';
import { AuthSessionService } from '../../../../core/auth/auth-session.service';
import { ApiConfigService } from '../../../../core/config/api-config.service';
import { maskIpv4Input, normalizeIpv4 } from '../../../../core/config/ip-address.util';
import { SettingsAccessService } from '../../../../core/config/settings-access.service';
import { NotificationService } from '../../../../core/feedback/notification.service';
import { AppHeaderComponent } from '../../../../shared/ui/app-header/app-header.component';
import { ConfigurationApiService } from '../../data-access/configuration-api.service';

type TestResult = 'none' | 'success' | 'failure';

@Component({
  selector: 'app-server-configuration-page',
  standalone: true,
  imports: [ReactiveFormsModule, IonContent, IonIcon, IonSpinner, AppHeaderComponent],
  templateUrl: './server-configuration.page.html',
  styleUrl: './server-configuration.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ServerConfigurationPage implements OnInit, OnDestroy {
  private readonly api = inject(ConfigurationApiService);
  readonly config = inject(ApiConfigService);
  private readonly access = inject(SettingsAccessService);
  private readonly session = inject(AuthSessionService);
  private readonly router = inject(Router);
  private readonly notifications = inject(NotificationService);

  readonly internalMode = this.config.hasSavedConfiguration();
  readonly ipControl = new FormControl(this.config.currentIp(), { nonNullable: true });
  readonly ipValue = signal(this.ipControl.value);
  readonly connectionState = signal<'checking' | 'online' | 'offline'>('offline');
  readonly result = signal<TestResult>('none');
  readonly invalidIp = signal(false);
  readonly testedIp = signal<string | null>(null);
  readonly saved = signal(false);
  readonly canProceed = computed(() => {
    const normalized = normalizeIpv4(this.ipValue());
    return this.connectionState() === 'online' && normalized !== null && normalized === this.testedIp();
  });

  constructor() {
    addIcons({
      checkmarkCircle, closeCircle, closeCircleOutline, cloudOfflineOutline,
      informationCircleOutline, lockClosedOutline, lockOpenOutline,
      saveOutline, serverOutline, wifiOutline
    });
  }

  ngOnInit(): void {
    if (this.internalMode) this.testConnection();
  }

  ngOnDestroy(): void {
    if (this.internalMode) this.access.lock();
  }

  updateIp(event: Event): void {
    const input = event.target as HTMLInputElement;
    const masked = maskIpv4Input(input.value);
    input.value = masked;
    this.ipControl.setValue(masked, { emitEvent: false });
    this.ipValue.set(masked);
    this.invalidateTest();
  }

  clearIp(): void {
    this.ipControl.setValue('');
    this.ipValue.set('');
    this.invalidateTest();
  }

  testConnection(): void {
    if (this.connectionState() === 'checking') return;
    const normalized = normalizeIpv4(this.ipValue());
    if (!normalized) {
      this.invalidIp.set(true);
      this.result.set('failure');
      return;
    }

    this.invalidIp.set(false);
    this.result.set('none');
    this.connectionState.set('checking');
    this.api.testConnection(normalized).pipe(
      finalize(() => {
        if (this.connectionState() === 'checking') this.connectionState.set('offline');
      })
    ).subscribe({
      next: () => {
        this.testedIp.set(normalized);
        this.connectionState.set('online');
        this.result.set('success');
      },
      error: () => {
        this.testedIp.set(null);
        this.connectionState.set('offline');
        this.result.set('failure');
      }
    });
  }

  async save(): Promise<void> {
    const ip = this.validatedIp();
    if (!ip) return;
    this.config.save(ip);
    this.saved.set(true);
    await this.notifications.show('Configuração salva com sucesso!', { color: 'success' });
  }

  async goToLogin(): Promise<void> {
    const ip = this.validatedIp();
    if (!ip) return;
    this.config.save(ip);
    this.access.lock();
    this.session.end();
    await this.router.navigateByUrl('/login', { replaceUrl: true });
  }

  private invalidateTest(): void {
    this.connectionState.set('offline');
    this.result.set('none');
    this.testedIp.set(null);
    this.invalidIp.set(false);
    this.saved.set(false);
  }

  private validatedIp(): string | null {
    if (!this.canProceed()) return null;
    return normalizeIpv4(this.ipValue());
  }
}
