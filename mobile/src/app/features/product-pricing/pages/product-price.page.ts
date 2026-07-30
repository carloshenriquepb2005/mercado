import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import type { OnInit } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import {
  IonBackButton,
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonInput,
  IonSpinner,
  IonTitle,
  IonToolbar,
  ToastController
} from '@ionic/angular/standalone';
import { finalize } from 'rxjs';
import { AppError } from '../../../core/models/app-error';
import { priceValidator } from '../../../shared/validators/price.validator';
import { ProdutoApiService } from '../data-access/produto-api.service';
import type { ProdutoResponse } from '../models/produto-response';
import { PricingSuccessService } from '../services/pricing-success.service';

@Component({
  selector: 'app-product-price-page',
  standalone: true,
  imports: [ReactiveFormsModule, IonBackButton, IonButton, IonButtons, IonContent, IonHeader,
    IonInput, IonSpinner, IonTitle, IonToolbar],
  templateUrl: './product-price.page.html',
  styleUrl: './product-price.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductPricePage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(ProdutoApiService);
  private readonly toasts = inject(ToastController);
  private readonly pricingSuccess = inject(PricingSuccessService);

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
    if (!/^\d{1,13}$/.test(code)) {
      this.loading.set(false);
      this.errorMessage.set('O código de barras informado é inválido.');
      return;
    }
    this.loadProduct(code);
  }

  appendKey(key: string): void {
    if (this.saving()) return;
    const current = this.form.controls.novoPreco.value;
    if (key === 'backspace') {
      this.form.controls.novoPreco.setValue(current.slice(0, -1));
      return;
    }
    if (key === ',' && (current.includes(',') || current.includes('.'))) return;
    const next = `${current}${key}`;
    if (/^\d{0,8}([.,]\d{0,2})?$/.test(next)) this.form.controls.novoPreco.setValue(next);
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
    void this.router.navigateByUrl('/scanner');
  }

  formatPrice(value: number): string {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value);
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
    const toast = await this.toasts.create({
      message: 'Preço atualizado com sucesso.',
      duration: 1800,
      position: 'bottom',
      color: 'primary'
    });
    await toast.present();
    await this.router.navigateByUrl('/sucesso');
  }

  private messageFrom(error: unknown): string {
    return error instanceof AppError ? error.userMessage : 'Ocorreu um erro inesperado.';
  }
}
