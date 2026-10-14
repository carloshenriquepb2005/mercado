import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { IonContent, IonIcon, IonSpinner } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import {
  chevronForwardOutline, closeOutline, eyeOffOutline, eyeOutline, serverOutline
} from 'ionicons/icons';
import { finalize } from 'rxjs';
import { SettingsAccessService } from '../../../../core/config/settings-access.service';
import { AppHeaderComponent } from '../../../../shared/ui/app-header/app-header.component';
import { BottomNavigationComponent } from '../../../../shared/ui/bottom-navigation/bottom-navigation.component';
import { ConfigurationApiService } from '../../data-access/configuration-api.service';

@Component({
  selector: 'app-settings-page',
  standalone: true,
  imports: [ReactiveFormsModule, IonContent, IonIcon, IonSpinner, AppHeaderComponent, BottomNavigationComponent],
  templateUrl: './settings.page.html',
  styleUrl: './settings.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SettingsPage {
  private readonly api = inject(ConfigurationApiService);
  private readonly access = inject(SettingsAccessService);
  private readonly router = inject(Router);

  readonly modalOpen = signal(false);
  readonly showPassword = signal(false);
  readonly submitting = signal(false);
  readonly errorMessage = signal('');
  readonly password = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(10)]
  });

  constructor() {
    addIcons({ chevronForwardOutline, closeOutline, eyeOffOutline, eyeOutline, serverOutline });
  }

  openServerConfiguration(): void {
    this.password.reset();
    this.errorMessage.set('');
    this.showPassword.set(false);
    this.modalOpen.set(true);
  }

  closeModal(): void {
    if (!this.submitting()) this.modalOpen.set(false);
  }

  togglePassword(): void {
    this.showPassword.update((visible) => !visible);
  }

  validatePassword(): void {
    if (this.password.invalid || this.submitting()) {
      this.password.markAsTouched();
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set('');
    this.api.validateCashierPassword(this.password.value).pipe(
      finalize(() => this.submitting.set(false))
    ).subscribe({
      next: () => {
        this.access.unlock();
        this.modalOpen.set(false);
        void this.router.navigateByUrl('/configuracao-servidor');
      },
      error: () => this.errorMessage.set('Senha do caixa incorreta.')
    });
  }
}
