import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

@Component({
  selector: 'app-numeric-keypad',
  standalone: true,
  templateUrl: './numeric-keypad.component.html',
  styleUrl: './numeric-keypad.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class NumericKeypadComponent {
  protected readonly keys = ['1', '2', '3', '4', '5', '6', '7', '8', '9', ',', '0'];
  readonly disabled = input(false);
  readonly keyPress = output<string>();
}
