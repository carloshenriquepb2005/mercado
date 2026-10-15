import { fakeAsync, TestBed, tick } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideIonicAngular } from '@ionic/angular/standalone';
import { AppComponent } from './app.component';

describe('AppComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [provideIonicAngular(), provideRouter([])]
    });
  });

  it('exibe a logo e o indicador de carregamento durante dois segundos', fakeAsync(() => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();

    const logo = fixture.nativeElement.querySelector('.splash-screen img') as HTMLImageElement;
    expect(logo.getAttribute('src')).toBe('assets/logo_com_fundo.png');
    expect(fixture.nativeElement.querySelector('ion-spinner')).not.toBeNull();

    tick(1999);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.splash-screen')).not.toBeNull();

    tick(1);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.splash-screen')).toBeNull();
  }));
});
