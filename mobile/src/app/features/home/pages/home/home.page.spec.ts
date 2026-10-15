import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { Platform, provideIonicAngular } from '@ionic/angular/standalone';
import { of } from 'rxjs';
import type { Subscription } from 'rxjs';
import { AuthSessionService } from '../../../../core/auth/auth-session.service';
import { ConnectivityService } from '../../../../core/connectivity/connectivity.service';
import { NotificationService } from '../../../../core/feedback/notification.service';
import { HomePage } from './home.page';

describe('HomePage', () => {
  const connectivity = {
    state: signal<'checking' | 'online' | 'offline'>('online'),
    check: jasmine.createSpy().and.returnValue(of(true))
  };
  const session = {
    user: signal({ displayName: 'Atendente' }),
    end: jasmine.createSpy()
  };
  const notifications = jasmine.createSpyObj<NotificationService>('notifications', ['show']);
  const backSubscription = jasmine.createSpyObj<Subscription>('backSubscription', ['unsubscribe']);
  const backButton = {
    subscribeWithPriority: jasmine.createSpy().and.returnValue(backSubscription)
  };
  const platform = { backButton };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HomePage],
      providers: [provideIonicAngular(), provideRouter([]),
        { provide: ConnectivityService, useValue: connectivity },
        { provide: AuthSessionService, useValue: session },
        { provide: NotificationService, useValue: notifications },
        { provide: Platform, useValue: platform }]
    });
    session.end.calls.reset();
    backButton.subscribeWithPriority.calls.reset();
    backSubscription.unsubscribe.calls.reset();
  });

  it('destaca consulta de estoque e mantém alterar preço como ação compacta', () => {
    const fixture = TestBed.createComponent(HomePage);
    fixture.detectChanges();

    const links = Array.from(fixture.nativeElement.querySelectorAll('a')) as HTMLAnchorElement[];
    const stockLink = links.find((link) => link.textContent?.includes('Consulta de Estoque'));
    const priceLink = links.find((link) => link.textContent?.includes('Alterar Preço'));

    expect(stockLink?.classList).toContain('featured');
    expect(stockLink?.getAttribute('href')).toBe('/scanner/consulta');
    expect(priceLink?.classList).toContain('compact');
    expect(priceLink?.getAttribute('href')).toBe('/scanner/alteracao');
    expect(fixture.nativeElement.querySelector('.sync-card')).toBeNull();
  });

  it('solicita confirmação antes de encerrar a sessão pelo botão', () => {
    spyOn(TestBed.inject(Router), 'navigateByUrl').and.resolveTo(true);
    const fixture = TestBed.createComponent(HomePage);
    fixture.detectChanges();

    (fixture.nativeElement.querySelector('.logout') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.logout-modal')).not.toBeNull();
    expect(session.end).not.toHaveBeenCalled();

    (fixture.nativeElement.querySelector('.confirm-logout') as HTMLButtonElement).click();
    expect(session.end).toHaveBeenCalledTimes(1);
  });

  it('abre a confirmação ao usar o botão voltar do celular na Home', () => {
    const fixture = TestBed.createComponent(HomePage);
    const page = fixture.componentInstance;
    page.ionViewDidEnter();

    expect(backButton.subscribeWithPriority).toHaveBeenCalledWith(10, jasmine.any(Function));
    const handleBackButton = backButton.subscribeWithPriority.calls.mostRecent().args[1];
    handleBackButton(() => undefined);

    expect(page.logoutModalOpen()).toBeTrue();
    expect(session.end).not.toHaveBeenCalled();
  });

  it('cancela a saída sem encerrar a sessão', () => {
    const fixture = TestBed.createComponent(HomePage);
    const page = fixture.componentInstance;
    page.requestLogout();
    page.cancelLogout();

    expect(page.logoutModalOpen()).toBeFalse();
    expect(session.end).not.toHaveBeenCalled();
  });
});
