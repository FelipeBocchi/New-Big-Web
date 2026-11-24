import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Observable, finalize, forkJoin, of, switchMap, tap } from 'rxjs';
import { apiErrorMessage } from '../../core/services/api';
import { ClienteService } from '../../services/clientes/cliente.service';
import { CashApiService, CashSession } from '../caixa/cash-api.service';
import { SaleApiService } from './sale-api.service';
import { PAYMENT_LABELS, PaymentMethod, SALE_STATUS_LABELS, SaleDetails, SaleProduct, SaleSummary } from './sale.types';

type CashAction = 'open' | 'close';

@Component({
  selector: 'app-pdv',
  imports: [CurrencyPipe, DatePipe, ReactiveFormsModule],
  templateUrl: './pdv.component.html',
  styleUrl: './pdv.component.scss',
})
export class PdvComponent implements OnInit {
  private readonly cashApi = inject(CashApiService);
  private readonly saleApi = inject(SaleApiService);
  private readonly clienteService = inject(ClienteService);
  private readonly destroyRef = inject(DestroyRef);

  readonly cashes = signal<CashSession[]>([]);
  readonly sales = signal<SaleSummary[]>([]);
  readonly cashId = signal<number | null>(null);
  readonly loaded = signal(false);
  readonly pending = signal(false);
  readonly needsRefresh = signal(false);
  readonly disabled = computed(() => !this.loaded() || this.pending() || this.needsRefresh());
  readonly feedback = signal<{ message: string; error: boolean } | null>(null);
  readonly cashAction = signal<CashAction | null>(null);
  readonly customers = signal<{ id: number; name: string }[]>([]);
  readonly products = signal<SaleProduct[]>([]);
  readonly creatingSale = signal(false);
  readonly detail = signal<SaleDetails | null>(null);
  readonly confirmingCancel = signal(false);

  readonly paymentLabels = PAYMENT_LABELS;
  readonly statusLabels = SALE_STATUS_LABELS;
  readonly paymentMethods = Object.entries(PAYMENT_LABELS) as [PaymentMethod, string][];
  readonly openCashes = computed(() => this.cashes().filter(cash => cash.status === 'OPEN'));
  readonly activeCash = computed(() => {
    const cashes = this.openCashes();
    return cashes.find(cash => cash.id === this.cashId()) ?? cashes[0] ?? null;
  });
  readonly displayedCash = computed(() => this.activeCash() ?? this.cashes()[0] ?? null);
  readonly pendingSales = computed(() => this.sales().filter(sale =>
    sale.status === 'OPENED' && sale.cashId === this.activeCash()?.id,
  ));
  readonly recentSales = computed(() => {
    const pending = new Set(this.pendingSales().map(sale => sale.id));
    return this.sales().filter((sale, index) => index < 12 || pending.has(sale.id));
  });
  readonly canEdit = computed(() => {
    const sale = this.detail();
    return sale?.status === 'OPENED' && this.openCashes().some(cash => cash.id === sale.cashId);
  });

  readonly cashForm = new FormGroup({ amount: new FormControl<number | null>(null, Validators.required) });
  readonly saleForm = new FormGroup({ customerId: new FormControl<number | null>(null) });
  readonly itemForm = new FormGroup({
    barcode: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.pattern(/\S/)] }),
    quantity: new FormControl<number | null>(1, [
      Validators.required, Validators.min(1), Validators.max(2_147_483_647), Validators.pattern(/^\d+$/),
    ]),
    discountUnit: new FormControl<number | null>(null, Validators.min(0)),
  });
  readonly paymentForm = new FormGroup({
    method: new FormControl<PaymentMethod | null>(null, Validators.required),
  });

  ngOnInit(): void {
    this.reload();
    this.saleApi.products().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: products => this.products.set(products),
      error: () => this.products.set([]),
    });
  }

  reload(): void {
    const selected = this.detail();
    this.run(forkJoin({
      cashes: this.cashApi.list(),
      sales: this.saleApi.list(),
      detail: selected ? this.saleApi.get(selected.id) : of(null),
    }), result => {
      this.cashes.set(result.cashes.sort((a, b) => b.id - a.id));
      this.sales.set(result.sales.sort((a, b) => b.id - a.id));
      this.detail.set(result.detail);
      this.needsRefresh.set(false);
      this.loaded.set(true);
      this.cashAction.set(null);
      if (!this.activeCash()) this.creatingSale.set(false);
    }, 'Não foi possível carregar o caixa e as vendas.');
  }

  selectCash(id: number): void {
    this.cashId.set(id);
    this.cashAction.set(null);
    this.creatingSale.set(false);
    this.detail.set(null);
  }

  startCash(action: CashAction): void {
    this.cashForm.reset();
    this.cashAction.set(action);
  }

  submitCash(): void {
    if (this.disabled()) return;
    const amount = this.cashForm.controls.amount.value;
    const action = this.cashAction();
    const cash = this.activeCash();

    if (amount === null || amount < (action === 'open' ? 0.01 : 0) ||
        amount > 9_999_999_999.99 || Number(amount.toFixed(2)) !== amount) {
      this.feedback.set({ message: 'Informe um valor em reais e centavos.', error: true });
      return;
    }
    if (action === 'open' && !cash) {
      this.run(this.cashApi.open(amount), opened => {
        this.updateCash(opened);
        this.cashId.set(opened.id);
        this.cashAction.set(null);
        this.success('Caixa aberto.');
      }, 'Não foi possível abrir o caixa.', true);
    } else if (action === 'close' && cash && !this.pendingSales().length) {
      this.run(this.cashApi.close(cash.id, amount), closed => {
        this.updateCash(closed);
        this.cashAction.set(null);
        this.creatingSale.set(false);
        this.success('Caixa fechado.');
      }, 'Não foi possível fechar o caixa.', true);
    }
  }

  startSale(): void {
    if (this.disabled() || !this.activeCash()) return;
    this.saleForm.reset();
    this.detail.set(null);
    this.creatingSale.set(true);
    this.clienteService.listAll().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: customers => this.customers.set(customers.filter(customer => customer.active)
        .map(customer => ({ id: customer.id, name: String(customer.name) }))
        .sort((a, b) => a.name.localeCompare(b.name, 'pt-BR'))),
      error: () => this.feedback.set({ message: 'A lista de clientes está indisponível. Você pode abrir a venda sem cliente.', error: true }),
    });
  }

  openSale(): void {
    const cash = this.activeCash();
    if (!cash || this.disabled()) return;
    this.run(this.saleApi.open(cash.id, this.saleForm.controls.customerId.value), sale => {
      this.updateSale(sale);
      this.showSale(sale);
      this.cashAction.set(null);
      this.success('Venda aberta.');
    }, 'Não foi possível abrir a venda.', true);
  }

  selectSale(id: number): void {
    this.run(this.saleApi.get(id), sale => {
      this.showSale(sale);
      if (sale.status === 'OPENED' && this.openCashes().some(cash => cash.id === sale.cashId)) {
        this.cashId.set(sale.cashId);
        this.cashAction.set(null);
      }
    }, 'Não foi possível consultar a venda.');
  }

  private showSale(sale: SaleDetails): void {
    this.resetEditorForms();
    this.creatingSale.set(false);
    this.detail.set(sale);
  }

  private resetEditorForms(): void {
    this.itemForm.reset({ quantity: 1 });
    this.paymentForm.reset();
    this.confirmingCancel.set(false);
  }

  addItem(): void {
    this.itemForm.markAllAsTouched();
    const sale = this.detail();
    const { barcode, quantity, discountUnit } = this.itemForm.getRawValue();
    if (!sale || this.disabled() || !this.canEdit() || this.itemForm.invalid || quantity === null) return;
    this.run(this.saleApi.addItem(sale, { barcode: barcode.trim(), quantity, discountUnit: discountUnit ?? 0 }).pipe(
      tap(() => this.needsRefresh.set(true)),
      switchMap(() => this.saleApi.get(sale.id)),
    ), updated => {
      this.updateSale(updated);
      this.detail.set(updated);
      this.itemForm.reset({ quantity: 1 });
      this.needsRefresh.set(false);
      this.success('Item adicionado.');
    }, 'Não foi possível atualizar os itens da venda.', true);
  }

  finalizeSale(): void {
    this.paymentForm.markAllAsTouched();
    const sale = this.detail();
    const method = this.paymentForm.controls.method.value;
    if (!sale || !method || this.disabled() || !this.canEdit() || sale.total <= 0) return;
    this.run(this.saleApi.close(sale, method).pipe(
      tap(closed => {
        this.updateSale(closed);
        this.detail.set({ ...closed, items: sale.items });
        this.needsRefresh.set(true);
      }),
      switchMap(closed => this.cashApi.get(closed.cashId)),
    ), cash => {
      this.updateCash(cash);
      this.needsRefresh.set(false);
      this.success('Venda finalizada.');
    }, 'Não foi possível atualizar o pagamento e o caixa.', true);
  }

  cancelSale(): void {
    const sale = this.detail();
    if (!sale || this.disabled() || sale.status !== 'OPENED') return;
    this.run(this.saleApi.cancel(sale), cancelled => {
      this.updateSale(cancelled);
      this.detail.set({ ...cancelled, items: sale.items });
      this.confirmingCancel.set(false);
      this.success('Venda cancelada.');
    }, 'Não foi possível cancelar a venda.', true);
  }

  private run<T>(request: Observable<T>, next: (value: T) => void, fallback: string, writes = false): void {
    if (this.pending()) return;
    this.pending.set(true);
    this.feedback.set(null);
    request.pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.pending.set(false))).subscribe({
      next,
      error: (error: unknown) => {
        if (writes && error instanceof HttpErrorResponse && (error.status === 0 || error.status >= 500)) {
          this.needsRefresh.set(true);
        }
        this.feedback.set({ message: apiErrorMessage(error, fallback), error: true });
      },
    });
  }

  private updateSale(sale: SaleSummary): void {
    this.sales.update(sales => [sale, ...sales.filter(previous => previous.id !== sale.id)].sort((a, b) => b.id - a.id));
  }

  private updateCash(cash: CashSession): void {
    this.cashes.update(cashes => [cash, ...cashes.filter(previous => previous.id !== cash.id)].sort((a, b) => b.id - a.id));
  }

  private success(message: string): void {
    this.feedback.set({ message, error: false });
  }
}
