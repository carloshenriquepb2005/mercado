import { Injectable, signal } from '@angular/core';
import type { AuthUser } from './auth-user';

const USER_KEY = 'precificacao.auth.user';
const REMEMBERED_USERNAME_KEY = 'precificacao.auth.rememberedUsername';

@Injectable({ providedIn: 'root' })
export class AuthSessionService {
  private readonly currentUser = signal<AuthUser | null>(this.restoreUser());
  readonly user = this.currentUser.asReadonly();

  isAuthenticated(): boolean {
    return this.currentUser() !== null;
  }

  start(user: AuthUser, rememberUsername: boolean): void {
    this.currentUser.set(user);
    this.writeStorage(sessionStorage, USER_KEY, JSON.stringify(user));
    if (rememberUsername) {
      this.writeStorage(localStorage, REMEMBERED_USERNAME_KEY, user.username);
    } else {
      this.removeStorage(localStorage, REMEMBERED_USERNAME_KEY);
    }
  }

  rememberedUsername(): string {
    return this.readStorage(localStorage, REMEMBERED_USERNAME_KEY) ?? '';
  }

  end(): void {
    this.currentUser.set(null);
    this.removeStorage(sessionStorage, USER_KEY);
  }

  private restoreUser(): AuthUser | null {
    const stored = this.readStorage(sessionStorage, USER_KEY);
    if (!stored) return null;
    try {
      const user = JSON.parse(stored) as Partial<AuthUser>;
      return typeof user.codigo === 'number'
        && typeof user.username === 'string'
        && typeof user.displayName === 'string'
        && (user.perfil === 1 || user.perfil === 2 || user.perfil === 3)
        ? { codigo: user.codigo, username: user.username, displayName: user.displayName, perfil: user.perfil }
        : null;
    } catch {
      return null;
    }
  }

  private readStorage(storage: Storage, key: string): string | null {
    try { return storage.getItem(key); } catch { return null; }
  }

  private writeStorage(storage: Storage, key: string, value: string): void {
    try { storage.setItem(key, value); } catch { /* armazenamento indisponível */ }
  }

  private removeStorage(storage: Storage, key: string): void {
    try { storage.removeItem(key); } catch { /* armazenamento indisponível */ }
  }
}
