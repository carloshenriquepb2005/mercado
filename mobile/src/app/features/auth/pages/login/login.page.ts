import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { IonButton, IonContent, IonSpinner } from '@ionic/angular/standalone';
import { finalize } from 'rxjs';
import { AuthSessionService } from '../../../../core/auth/auth-session.service';
import { NotificationService } from '../../../../core/feedback/notification.service';
import { AppError } from '../../../../core/http/app-error';
import { AuthApiService } from '../../data-access/auth-api.service';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [ReactiveFormsModule, IonButton, IonContent, IonSpinner],
  templateUrl: './login.page.html',
  styleUrl: './login.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoginPage {
  private readonly api = inject(AuthApiService);
  private readonly session = inject(AuthSessionService);
  private readonly router = inject(Router);
  private readonly notifications = inject(NotificationService);

  readonly submitting = signal(false);
  readonly showPassword = signal(false);
  readonly errorMessage = signal('');
  readonly form = new FormGroup({
    username: new FormControl(this.session.rememberedUsername(), {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(80)]
    }),
    password: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(120)]
    }),
    remember: new FormControl(this.session.rememberedUsername().length > 0, { nonNullable: true })
  });

  submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }
    const { username, password, remember } = this.form.getRawValue();
    this.submitting.set(true);
    this.errorMessage.set('');
    this.api.login(username.trim(), password).pipe(
      finalize(() => this.submitting.set(false))
    ).subscribe({
      next: (user) => {
        this.session.start(user, remember);
        void this.router.navigateByUrl('/home', { replaceUrl: true });
      },
      error: (error: unknown) => {
        this.errorMessage.set(error instanceof AppError
          ? error.userMessage
          : 'Não foi possível entrar. Tente novamente.');
      }
    });
  }

  togglePassword(): void {
    this.showPassword.update((visible) => !visible);
  }

  async forgotPassword(): Promise<void> {
    await this.notifications.show(
      'Solicite a redefinição da senha ao responsável pelo sistema.',
      { duration: 3200 }
    );
  }
}
