import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class SettingsAccessService {
  private readonly allowed = signal(false);

  unlock(): void {
    this.allowed.set(true);
  }

  lock(): void {
    this.allowed.set(false);
  }

  isUnlocked(): boolean {
    return this.allowed();
  }

  consume(): boolean {
    const unlocked = this.allowed();
    this.allowed.set(false);
    return unlocked;
  }
}
