import { TestBed } from '@angular/core/testing';
import { ToastController } from '@ionic/angular/standalone';
import { NotificationService } from './notification.service';

describe('NotificationService', () => {
  const present = jasmine.createSpy().and.resolveTo();
  const toasts = { create: jasmine.createSpy().and.resolveTo({ present }) };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [NotificationService, { provide: ToastController, useValue: toasts }]
    });
    toasts.create.calls.reset();
    present.calls.reset();
  });

  it('apresenta toast com padrões seguros', async () => {
    await TestBed.inject(NotificationService).show('Mensagem');
    expect(toasts.create).toHaveBeenCalledWith({
      message: 'Mensagem', duration: 3000, position: 'bottom', color: 'primary'
    });
    expect(present).toHaveBeenCalled();
  });

  it('permite ajustar duração e cor', async () => {
    await TestBed.inject(NotificationService).show('Erro', { color: 'danger', duration: 1800 });
    expect(toasts.create).toHaveBeenCalledWith({
      message: 'Erro', duration: 1800, position: 'bottom', color: 'danger'
    });
  });

  it('apresenta confirmações com a cor de sucesso', async () => {
    await TestBed.inject(NotificationService).show('Salvo', { color: 'success' });
    expect(toasts.create).toHaveBeenCalledWith({
      message: 'Salvo', duration: 3000, position: 'bottom', color: 'success'
    });
  });
});
