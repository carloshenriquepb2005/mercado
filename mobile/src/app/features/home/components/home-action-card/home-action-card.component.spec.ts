import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { addIcons } from 'ionicons';
import { calculatorOutline, pricetagOutline } from 'ionicons/icons';
import { HomeActionCardComponent } from './home-action-card.component';

describe('HomeActionCardComponent', () => {
  beforeEach(() => {
    addIcons({ calculatorOutline, pricetagOutline });
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
  });

  it('emite a seleção de uma ação habilitada', () => {
    const fixture = TestBed.createComponent(HomeActionCardComponent);
    fixture.componentRef.setInput('title', 'Promoções');
    fixture.componentRef.setInput('icon', 'calculator-outline');
    let selected = false;
    fixture.componentInstance.selected.subscribe(() => selected = true);
    fixture.detectChanges();
    (fixture.nativeElement.querySelector('button') as HTMLButtonElement).click();
    expect(selected).toBeTrue();
  });

  it('desabilita ações indisponíveis', () => {
    const fixture = TestBed.createComponent(HomeActionCardComponent);
    fixture.componentRef.setInput('title', 'Etiquetas');
    fixture.componentRef.setInput('icon', 'pricetag-outline');
    fixture.componentRef.setInput('disabled', true);
    fixture.detectChanges();
    expect((fixture.nativeElement.querySelector('button') as HTMLButtonElement).disabled).toBeTrue();
  });
});
