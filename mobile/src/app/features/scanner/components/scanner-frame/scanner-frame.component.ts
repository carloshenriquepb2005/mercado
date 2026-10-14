import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-scanner-frame',
  standalone: true,
  templateUrl: './scanner-frame.component.html',
  styleUrl: './scanner-frame.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ScannerFrameComponent {}
