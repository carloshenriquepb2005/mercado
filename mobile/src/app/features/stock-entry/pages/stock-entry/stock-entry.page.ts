import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import type { OnInit } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { IonButton, IonContent, IonInput, IonSpinner } from '@ionic/angular/standalone';
import { finalize } from 'rxjs';
import { NotificationService } from '../../../../core/feedback/notification.service';
import { AppError } from '../../../../core/http/app-error';
import { BrlCurrencyPipe } from '../../../../shared/pipes/brl-currency.pipe';
import { AppHeaderComponent } from '../../../../shared/ui/app-header/app-header.component';
import { BarcodeSymbolComponent } from '../../../../shared/ui/barcode-symbol/barcode-symbol.component';
import { PageStateComponent } from '../../../../shared/ui/page-state/page-state.component';
import { isValidProductCode } from '../../../../shared/utils/product-code.util';
import { maskPriceDigits, parseDecimal } from '../../../../shared/utils/price-mask.util';
import { optionalIsoDateValidator } from '../../../../shared/validators/iso-date.validator';
import { priceValidator } from '../../../../shared/validators/price.validator';
import { quantityValidator } from '../../../../shared/validators/quantity.validator';
import { ProdutoApiService } from '../../../product-pricing/data-access/produto-api.service';
import type { EntradaEstoqueResponse } from '../../../product-pricing/models/entrada-estoque';
import type { ProdutoResponse } from '../../../product-pricing/models/produto-response';
import { StockEntrySuccessStore } from '../../state/stock-entry-success.store';

export const REFRESH_NOTICE = 'Confira o estoque atual antes de registrar novamente.';

@Component({
  selector: 'app-stock-entry-page',
  standalone: true,
  imports: [ReactiveFormsModule, IonButton, IonContent, IonInput, IonSpinner,
    AppHeaderComponent, BarcodeSymbolComponent, BrlCurrencyPipe, PageStateComponent],
  templateUrl: './stock-entry.page.html',
  styleUrl: './stock-entry.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class StockEntryPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(ProdutoApiService);
  private readonly notifications = inject(NotificationService);
  private readonly successStore = inject(StockEntrySuccessStore);

  readonly product = signal<ProdutoResponse | null>(null);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly refreshing = signal(false);
  /** Após erro sem confirmação de gravação, o envio só é liberado depois de recarregar o produto. */
  readonly awaitingRefresh = signal(false);
  readonly errorMessage = signal('');
  readonly refreshNotice = signal('');

  readonly form = new FormGroup({
    quantidade: new FormControl('', { nonNullable: true, validators: [Validators.required, quantityValidator] }),
    precoCusto: new FormControl('', { nonNullable: true, validators: [Validators.required, priceValidator] }),
    dataVencimento: new FormControl('', { nonNullable: true, validators: [optionalIsoDateValidator] })
  });

  private readonly quantityText = toSignal(this.form.controls.quantidade.valueChanges, { initialValue: '' });
  private readonly costText = signal('');

  readonly projectedStock = computed<number | null>(() => {
    const product = this.product();
    const raw = this.quantityText();
    if (!product || !raw || quantityValidator(this.form.controls.quantidade)) return null;
    return Math.round(((product.estoqueAtual ?? 0) + parseDecimal(raw)) * 1000) / 1000;
  });

  readonly costAboveSalePrice = computed(() => {
    const product = this.product();
    const raw = this.costText();
    if (!product || product.precoVenda === null || !raw || priceValidator(this.form.controls.precoCusto)) return false;
    return parseDecimal(raw) > product.precoVenda;
  });

  private code = '';

  ngOnInit(): void {
    this.code = this.route.snapshot.paramMap.get('codigo') ?? '';
    if (!isValidProductCode(this.code)) {
      this.loading.set(false);
      this.errorMessage.set('O código de barras informado é inválido.');
      return;
    }
    this.loadProduct();
  }

  formatCost(rawValue: string | null | undefined): void {
    if (this.saving()) return;
    const masked = maskPriceDigits(rawValue);
    this.form.controls.precoCusto.setValue(masked, { emitEvent: false });
    this.costText.set(masked);
  }

  save(): void {
    const product = this.product();
    if (!product || this.form.invalid || this.saving() || this.awaitingRefresh() || this.refreshing()) {
      this.form.markAllAsTouched();
      return;
    }

    const { quantidade, precoCusto, dataVencimento } = this.form.getRawValue();
    const quantity = parseDecimal(quantidade);
    const cost = parseDecimal(precoCusto);
    const expiry = dataVencimento.trim();
    this.saving.set(true);
    this.errorMessage.set('');
    this.refreshNotice.set('');
    this.api.registrarEntradaEstoque(product.id, {
      quantidade: quantity,
      precoCusto: cost,
      ...(expiry ? { dataVencimento: expiry } : {})
    }).pipe(
      finalize(() => this.saving.set(false))
    ).subscribe({
      next: (result) => void this.showSuccess(product, result),
      error: (error: unknown) => this.handleSaveError(error)
    });
  }

  refresh(): void {
    this.loadProduct();
  }

  cancel(): void {
    void this.router.navigateByUrl('/scanner/entrada');
  }

  private loadProduct(): void {
    const isFirstLoad = this.product() === null;
    if (isFirstLoad) this.loading.set(true);
    this.refreshing.set(!isFirstLoad);
    this.api.buscarPorCodigo(this.code).pipe(
      finalize(() => {
        this.loading.set(false);
        this.refreshing.set(false);
      })
    ).subscribe({
      next: (product) => {
        this.product.set(product);
        this.awaitingRefresh.set(false);
      },
      error: (error: unknown) => {
        this.errorMessage.set(isFirstLoad || !this.awaitingRefresh()
          ? this.messageFrom(error)
          : `${this.messageFrom(error)} Não foi possível conferir o estoque atual.`);
      }
    });
  }

  /**
   * Erros 400/404 indicam que nada foi gravado. Qualquer outro erro (rede, timeout, 5xx) é ambíguo:
   * a entrada pode ter sido gravada, então o produto é recarregado, a quantidade é limpa e o
   * novo envio exige confirmação do atendente. Nunca há reenvio automático.
   */
  private handleSaveError(error: unknown): void {
    this.errorMessage.set(this.messageFrom(error));
    if (error instanceof AppError && (error.status === 400 || error.status === 404)) return;

    this.awaitingRefresh.set(true);
    this.refreshNotice.set(REFRESH_NOTICE);
    this.form.controls.quantidade.reset('');
    this.form.controls.dataVencimento.reset('');
    this.form.markAsUntouched();
    this.loadProduct();
  }

  private async showSuccess(product: ProdutoResponse, result: EntradaEstoqueResponse): Promise<void> {
    this.successStore.set({
      description: product.descricao,
      barcode: product.codigoBarras,
      unit: product.unidade,
      previousStock: result.estoqueAnterior,
      currentStock: result.estoqueAtual,
      previousCost: result.precoCustoAnterior,
      currentCost: result.precoCustoAtual
    });
    await this.notifications.show('Entrada no estoque registrada com sucesso.', { color: 'success', duration: 1800 });
    await this.router.navigateByUrl('/entrada-estoque-sucesso');
  }

  private messageFrom(error: unknown): string {
    return error instanceof AppError ? error.userMessage : 'Ocorreu um erro inesperado.';
  }
}
