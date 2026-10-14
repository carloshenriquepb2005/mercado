import { TestBed } from '@angular/core/testing';
import { ConnectionStatusComponent } from './connection-status.component';

describe('ConnectionStatusComponent', () => {
  it('apresenta o estado e emite nova tentativa quando interativo', () => {
    const fixture = TestBed.createComponent(ConnectionStatusComponent);
    fixture.componentRef.setInput('state', 'online');
    fixture.componentRef.setInput('interactive', true);
    let retried = false;
    fixture.componentInstance.retry.subscribe(() => retried = true);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Servidor Online');
    (fixture.nativeElement.querySelector('button') as HTMLButtonElement).click();
    expect(retried).toBeTrue();
  });

  it('usa rótulo curto no cabeçalho', () => {
    const fixture = TestBed.createComponent(ConnectionStatusComponent);
    fixture.componentRef.setInput('state', 'offline');
    fixture.componentRef.setInput('compact', true);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent.trim()).toBe('Offline');
  });
});
