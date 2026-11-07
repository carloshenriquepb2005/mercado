import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { provideIonicAngular } from '@ionic/angular/standalone';
import { StockEntrySuccessStore } from '../../state/stock-entry-success.store';
import { StockEntrySuccessPage } from './stock-entry-success.page';

describe('StockEntrySuccessPage', () => {
  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      imports: [StockEntrySuccessPage],
      providers: [provideIonicAngular(), provideRouter([])]
    });
  });

  afterEach(() => sessionStorage.clear());

  it('exibe estoque e custo anteriores e atuais da entrada', () => {
    TestBed.inject(StockEntrySuccessStore).set({
      description: 'Produto', barcode: '00123', unit: 'UN',
      previousStock: 18.5, currentStock: 31, previousCost: 6.9, currentCost: 7.35
    });
    const fixture = TestBed.createComponent(StockEntrySuccessPage);
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    for (const expected of ['Entrada registrada com sucesso!', 'Produto', '00123',
      '18.5 UN', '31 UN', 'Estoque anterior', 'Novo custo']) {
      expect(text).toContain(expected);
    }
  });

  it('exibe traço quando o custo anterior era nulo', () => {
    TestBed.inject(StockEntrySuccessStore).set({
      description: 'Produto', barcode: '00123', unit: 'UN',
      previousStock: 0, currentStock: 5, previousCost: null, currentCost: 7.35
    });
    const fixture = TestBed.createComponent(StockEntrySuccessPage);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('—');
  });

  it('mostra mensagem genérica quando não há dados', () => {
    const fixture = TestBed.createComponent(StockEntrySuccessPage);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('A entrada foi concluída.');
  });

  it('limpa os dados e navega ao registrar outra entrada ou voltar para a Home', () => {
    const navigate = spyOn(TestBed.inject(Router), 'navigateByUrl').and.resolveTo(true);
    const store = TestBed.inject(StockEntrySuccessStore);
    const page = TestBed.createComponent(StockEntrySuccessPage).componentInstance;

    store.set({ description: 'P', barcode: '1', unit: null, previousStock: 0, currentStock: 1,
      previousCost: null, currentCost: 1 });
    page.newEntry();
    expect(navigate).toHaveBeenCalledWith('/scanner/entrada');
    expect(store.data()).toBeNull();

    page.goHome();
    expect(navigate).toHaveBeenCalledWith('/home');
  });
});
