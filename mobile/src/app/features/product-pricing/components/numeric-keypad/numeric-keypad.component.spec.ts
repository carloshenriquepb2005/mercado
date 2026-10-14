import { TestBed } from '@angular/core/testing';
import { NumericKeypadComponent } from './numeric-keypad.component';

describe('NumericKeypadComponent', () => {
  it('emite a tecla selecionada', () => {
    const fixture = TestBed.createComponent(NumericKeypadComponent);
    const emitted: string[] = [];
    fixture.componentInstance.keyPress.subscribe((key) => emitted.push(key));
    fixture.detectChanges();
    const firstButton = fixture.nativeElement.querySelector('button') as HTMLButtonElement;
    firstButton.click();
    expect(emitted).toEqual(['1']);
  });
});
