import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import type { OnInit } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import {
  IonButton,
  IonContent,
  IonInput,
  IonSpinner,
} from '@ionic/angular/standalone';
import { finalize } from 'rxjs';
import { NotificationService } from '../../../../core/feedback/notification.service';
import { AppError } from '../../../../core/http/app-error';
import { AppHeaderComponent } from '../../../../shared/ui/app-header/app-header.component';
import { PageStateComponent } from '../../../../shared/ui/page-state/page-state.component';
import { isValidProductCode } from '../../../../shared/utils/product-code.util';
import { priceValidator } from '../../../../shared/validators/price.validator';
import { ProductCardComponent } from '../../components/product-card/product-card.component';
import { ProdutoApiService } from '../../data-access/produto-api.service';
import type { ProdutoResponse } from '../../models/produto-response';
import { PricingSuccessStore } from '../../state/pricing-success.store';

@Component({
  selector: 'app-product-price-page',
  standalone: true,
  imports: [ReactiveFormsModule, IonButton, IonContent, IonInput, IonSpinner,
    AppHeaderComponent, PageStateComponent, ProductCardComponent],
  templateUrl: './product-price.page.html',
  styleUrl: './product-price.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductPricePage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(ProdutoApiService);
  private readonly notifications = inject(NotificationService);
  private readonly pricingSuccess = inject(PricingSuccessStore);

  readonly product = signal<ProdutoResponse | null>(null);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly errorMessage = signal('');
  readonly previousPrice = signal(0);

  readonly form = new FormGroup({
    novoPreco: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, priceValidator]
    })
  });

  ngOnInit(): void {
    const code = this.route.snapshot.paramMap.get('codigo') ?? '';
    if (!isValidProductCode(code)) {
      this.loading.set(false);
      this.errorMessage.set('O código de barras informado é inválido.');
      return;
    }
    this.loadProduct(code);
  }

  formatPrice(rawValue: string | null | undefined): void {
    if (this.saving()) return;

    const digits = String(rawValue ?? '')
      .replace(/\D/g, '')
      .replace(/^0+/, '')
      .slice(0, 10);

    if (!digits) {
      this.form.controls.novoPreco.setValue('', { emitEvent: false });
      return;
    }

    const paddedDigits = digits.padStart(3, '0');
    const integerPart = paddedDigits.slice(0, -2);
    const decimalPart = paddedDigits.slice(-2);
    this.form.controls.novoPreco.setValue(`${integerPart},${decimalPart}`, { emitEvent: false });
  }

  save(): void {
    const product = this.product();
    if (!product || this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }

    const newPrice = Number(this.form.controls.novoPreco.value.replace(',', '.'));
    this.saving.set(true);
    this.errorMessage.set('');
    this.api.atualizarPreco(product.id, newPrice).pipe(
      finalize(() => this.saving.set(false))
    ).subscribe({
      next: () => void this.showSuccess(product, newPrice),
      error: (error: unknown) => this.errorMessage.set(this.messageFrom(error))
    });
  }

  cancel(): void {
    void this.router.navigateByUrl('/scanner/alteracao');
  }

  private loadProduct(code: string): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.api.buscarPorCodigo(code).pipe(
      finalize(() => this.loading.set(false))
    ).subscribe({
      next: (product) => {
        this.product.set(product);
        this.previousPrice.set(product.precoVenda);
      },
      error: (error: unknown) => this.errorMessage.set(this.messageFrom(error))
    });
  }

  private async showSuccess(product: ProdutoResponse, newPrice: number): Promise<void> {
    this.pricingSuccess.set({
      description: product.descricao,
      barcode: product.codigoBarras,
      previousPrice: this.previousPrice(),
      newPrice
    });
    await this.notifications.show('Preço atualizado com sucesso.', { color: 'success', duration: 1800 });
    await this.router.navigateByUrl('/sucesso');
  }

  private messageFrom(error: unknown): string {
    return error instanceof AppError ? error.userMessage : 'Ocorreu um erro inesperado.';
  }
}
