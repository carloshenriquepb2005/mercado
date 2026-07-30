import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { provideIonicAngular } from '@ionic/angular/standalone';
import { PricingSuccessService } from '../services/pricing-success.service';
import { PricingSuccessPage } from './pricing-success.page';

describe('PricingSuccessPage', () => {
  const success = jasmine.createSpyObj<PricingSuccessService>('success', ['clear'], {
    data: signal({ description: 'Produto', barcode: '00123', previousPrice: 9.5, newPrice: 10.5 })
  });
  const router = jasmine.createSpyObj<Router>('router', ['navigateByUrl']);

  beforeEach(() => {
    router.navigateByUrl.and.resolveTo(true);
    TestBed.configureTestingModule({
      imports: [PricingSuccessPage],
      providers: [provideIonicAngular(),
        { provide: PricingSuccessService, useValue: success },
        { provide: Router, useValue: router }]
    });
  });

  afterEach(() => {
    success.clear.calls.reset();
    router.navigateByUrl.calls.reset();
  });

  it('formata o resumo e permite escanear outro produto', () => {
    const page = TestBed.createComponent(PricingSuccessPage).componentInstance;
    expect(page.formatPrice(10.5)).toContain('10,50');
    page.scanAnother();
    expect(success.clear).toHaveBeenCalled();
    expect(router.navigateByUrl).toHaveBeenCalledWith('/scanner');
  });

  it('permite voltar para a home', () => {
    const page = TestBed.createComponent(PricingSuccessPage).componentInstance;
    page.goHome();
    expect(router.navigateByUrl).toHaveBeenCalledWith('/home');
  });
});
