import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideIonicAngular } from '@ionic/angular/standalone';
import { of } from 'rxjs';
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

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HomePage],
      providers: [provideIonicAngular(), provideRouter([]),
        { provide: ConnectivityService, useValue: connectivity },
        { provide: AuthSessionService, useValue: session },
        { provide: NotificationService, useValue: notifications }]
    });
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
});
