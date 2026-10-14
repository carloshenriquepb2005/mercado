import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import type { OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { IonButton, IonContent } from '@ionic/angular/standalone';
import { finalize } from 'rxjs';
import { AppError } from '../../../../core/http/app-error';
import { BrlCurrencyPipe } from '../../../../shared/pipes/brl-currency.pipe';
import { AppHeaderComponent } from '../../../../shared/ui/app-header/app-header.component';
import { BarcodeSymbolComponent } from '../../../../shared/ui/barcode-symbol/barcode-symbol.component';
import { PageStateComponent } from '../../../../shared/ui/page-state/page-state.component';
import { isValidProductCode } from '../../../../shared/utils/product-code.util';
import { ProdutoApiService } from '../../data-access/produto-api.service';
import type { ProdutoResponse } from '../../models/produto-response';

@Component({
  selector: 'app-product-stock-page',
  standalone: true,
  imports: [IonButton, IonContent, AppHeaderComponent, BarcodeSymbolComponent,
    BrlCurrencyPipe, PageStateComponent],
  templateUrl: './product-stock.page.html',
  styleUrl: './product-stock.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductStockPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(ProdutoApiService);

  readonly product = signal<ProdutoResponse | null>(null);
  readonly loading = signal(true);
  readonly errorMessage = signal('');

  ngOnInit(): void {
    const code = this.route.snapshot.paramMap.get('codigo') ?? '';
    if (!isValidProductCode(code)) {
      this.loading.set(false);
      this.errorMessage.set('O código de barras informado é inválido.');
      return;
    }
    this.loadProduct(code);
  }

  scanAnother(): void {
    void this.router.navigateByUrl('/scanner/consulta');
  }

  private loadProduct(code: string): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.api.buscarPorCodigo(code).pipe(
      finalize(() => this.loading.set(false))
    ).subscribe({
      next: (product) => this.product.set(product),
      error: (error: unknown) => this.errorMessage.set(this.messageFrom(error))
    });
  }

  private messageFrom(error: unknown): string {
    return error instanceof AppError ? error.userMessage : 'Ocorreu um erro inesperado.';
  }
}
