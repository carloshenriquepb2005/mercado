import { TestBed } from '@angular/core/testing';
import { provideIonicAngular } from '@ionic/angular/standalone';
import { AppHeaderComponent } from './app-header.component';

describe('AppHeaderComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AppHeaderComponent],
      providers: [provideIonicAngular()]
    });
  });

  it('exibe a marca SisBac no header padrão', () => {
    const fixture = TestBed.createComponent(AppHeaderComponent);
    fixture.detectChanges();

    const logo = fixture.nativeElement.querySelector('.brand-logo') as HTMLImageElement;
    expect(logo).not.toBeNull();
    expect(logo.getAttribute('src')).toBe('assets/texto_logo_sem_fundo.png');
    expect(logo.getAttribute('alt')).toBe('SisBac');
  });

  it('preserva título específico e botão de voltar', () => {
    const fixture = TestBed.createComponent(AppHeaderComponent);
    fixture.componentRef.setInput('title', 'Ajustes');
    fixture.componentRef.setInput('backHref', '/home');
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Ajustes');
    expect(fixture.nativeElement.querySelector('.brand-logo')).toBeNull();
    const backButton = fixture.nativeElement.querySelector('ion-back-button') as HTMLIonBackButtonElement;
    expect(backButton.defaultHref).toBe('/home');
  });
});
