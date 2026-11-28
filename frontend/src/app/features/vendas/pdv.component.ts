import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { catchError, EMPTY, finalize, forkJoin, Observable, of, retry, switchMap, tap } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { apiErrorMessage } from '../../core/services/api';
import { ClienteService } from '../../services/clientes/cliente.service';
import { CashApiService, CashSession } from '../caixa/cash-api.service';
import { CashAction, CashDialogComponent } from '../caixa/cash-dialog/cash-dialog.component';
import { CashPanelComponent } from '../caixa/cash-panel/cash-panel.component';
import { SaleApiService } from './sale-api.service';
import { SaleEditorComponent } from './sale-editor/sale-editor.component';
import { SalesHistoryComponent } from './sale-history/sale-history.component';
import { PaymentMethod, SaleDetails, SaleItemInput, SaleProduct, SaleSummary } from './sale.types';

interface PdvSnapshot {
  cashes: CashSession[];
  sales: SaleSummary[];
  sale: SaleDetails | null;
}

interface Notice {
  message: string;
  error: boolean;
}

@Component({
  selector: 'app-pdv',
  imports: [
    CashDialogComponent,
    CashPanelComponent,
    ReactiveFormsModule,
    SaleEditorComponent,
    SalesHistoryComponent,
  ],
  templateUrl: './pdv.component.html',
  styleUrl: './pdv.component.scss',
})
export class PdvComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly cashApi = inject(CashApiService);
  private readonly saleApi = inject(SaleApiService);
  private readonly customerService = inject(ClienteService);
  private readonly destroyRef = inject(DestroyRef);

  private readonly cashes = signal<CashSession[]>([]);
  private readonly sales = signal<SaleSummary[]>([]);
  private readonly selectedCashId = signal<number | null>(null);
  private readonly needsSync = signal(false);

  readonly loaded = signal(false);
  readonly busy = signal(false);
  readonly notice = signal<Notice | null>(null);
  readonly cashAction = signal<CashAction | null>(null);

  readonly customers = signal<{ id: number; name: string }[]>([]);
  readonly products = signal<SaleProduct[]>([]);
  readonly creatingSale = signal(false);
  readonly selectedSale = signal<SaleDetails | null>(null);

  readonly saleForm = new FormGroup({
    customerId: new FormControl<number | null>(null),
  });

  readonly disabled = computed(() => !this.loaded() || this.busy() || this.needsSync());

  readonly cashError = computed(() => {
    const notice = this.notice();
    return notice?.error ? notice.message : '';
  });

  readonly openCashes = computed(() => this.cashes().filter(cash => cash.status === 'OPEN'));

  readonly activeCash = computed(() => {
    const open = this.openCashes();
    return open.find(cash => cash.id === this.selectedCashId()) ?? open.at(0) ?? null;
  });

  readonly displayedCash = computed(() => this.activeCash() ?? this.cashes().at(0) ?? null);

  readonly openSales = computed(() => {
    const cashId = this.activeCash()?.id;
    return this.sales().filter(sale => sale.status === 'OPENED' && sale.cashId === cashId);
  });

  readonly recentSales = computed(() => {
    const openIds = new Set(this.openSales().map(sale => sale.id));
    return this.sales().filter((sale, index) => index < 12 || openIds.has(sale.id));
  });

  readonly canEditSale = computed(() => {
    const sale = this.selectedSale();
    return sale?.status === 'OPENED' && this.openCashes().some(cash => cash.id === sale.cashId);
  });

  ngOnInit(): void {
    this.run(
      this.fetchSnapshot(),
      snapshot => this.applySnapshot(snapshot),
      'Não foi possível carregar o caixa e as vendas.',
    );

    this.saleApi.products().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: products => this.products.set(products),
      error: () => this.products.set([]),
    });
  }

  selectCash(id: number): void {
    this.selectedCashId.set(id);
    this.showHistory();
  }

  showCashDialog(action: CashAction): void {
    this.notice.set(null);
    this.cashAction.set(action);
  }

  confirmCash(amount: number): void {
    if (this.disabled()) return;

    const employeeId = this.requireEmployeeId();
    if (employeeId === null) return;

    const action = this.cashAction();
    const cash = this.activeCash();

    if (action === 'open' && !cash) {
      this.run(this.cashApi.open({ openingAmount: amount, employeeId }), opened => {
        this.updateCash(opened);
        this.selectedCashId.set(opened.id);
        this.cashAction.set(null);
        this.showSuccess('Caixa aberto.');
      }, 'Não foi possível abrir o caixa.', true);
    } else if (action === 'close' && cash && !this.openSales().length) {
      this.run(this.cashApi.close(cash.id, { closingAmount: amount, employeeId }), closed => {
        this.updateCash(closed);
        this.cashAction.set(null);
        this.showHistory();
        this.showSuccess('Caixa fechado.');
      }, 'Não foi possível fechar o caixa.', true);
    }
  }

  startSale(): void {
    if (this.disabled() || !this.activeCash()) return;

    this.saleForm.reset();
    this.notice.set(null);
    this.selectedSale.set(null);
    this.creatingSale.set(true);

    this.customerService.listAll().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: customers => {
        const activeCustomers = customers
          .filter(customer => customer.active)
          .map(customer => ({ id: customer.id, name: String(customer.name) }))
          .sort((a, b) => a.name.localeCompare(b.name, 'pt-BR'));

        this.customers.set(activeCustomers);
      },
      error: () => {
        this.customers.set([]);
        if (this.creatingSale()) {
          this.notice.set({
            message: 'A lista de clientes está indisponível. Você pode abrir a venda sem cliente.',
            error: true,
          });
        }
      },
    });
  }

  openSale(): void {
    const cash = this.activeCash();
    if (!cash || this.disabled()) return;

    const employeeId = this.requireEmployeeId();
    if (employeeId === null) return;

    const customerId = this.saleForm.controls.customerId.value;

    this.run(this.saleApi.open({ cashId: cash.id, employeeId, customerId }), sale => {
      this.updateSale(sale);
      this.selectedSale.set(sale);
      this.creatingSale.set(false);
      this.showSuccess('Venda aberta.');
    }, 'Não foi possível abrir a venda.', true);
  }

  selectSale(id: number): void {
    this.run(this.saleApi.get(id), sale => {
      this.selectedSale.set(sale);

      if (sale.status === 'OPENED' && this.openCashes().some(cash => cash.id === sale.cashId)) {
        this.selectedCashId.set(sale.cashId);
      }
    }, 'Não foi possível consultar a venda.');
  }

  showHistory(): void {
    this.creatingSale.set(false);
    this.selectedSale.set(null);
  }

  addItem(item: SaleItemInput): void {
    const sale = this.selectedSale();
    if (!sale || this.disabled() || !this.canEditSale()) return;

    const request = this.saleApi.addItem(sale, item).pipe(
      tap(() => this.needsSync.set(true)),
      switchMap(() => this.saleApi.get(sale.id)),
    );

    this.run(request, updatedSale => {
      this.updateSale(updatedSale);
      this.selectedSale.set(updatedSale);
      this.needsSync.set(false);
      this.showSuccess('Item adicionado.');
    }, 'Não foi possível atualizar os itens da venda.', true);
  }

  finishSale(payment: PaymentMethod): void {
    const sale = this.selectedSale();
    if (!sale || this.disabled() || !this.canEditSale() || sale.total <= 0) return;

    const request = this.saleApi.close(sale, payment).pipe(
      tap(closedSale => {
        this.updateSale(closedSale);
        this.showHistory();
        this.needsSync.set(true);
      }),
      switchMap(closedSale => this.cashApi.get(closedSale.cashId)),
    );

    this.run(request, cash => {
      this.updateCash(cash);
      this.needsSync.set(false);
      this.showSuccess('Venda finalizada.');
    }, 'Não foi possível atualizar o pagamento e o caixa.', true);
  }

  cancelSale(): void {
    const sale = this.selectedSale();
    if (!sale || this.disabled() || sale.status !== 'OPENED') return;

    this.run(this.saleApi.cancel(sale), cancelledSale => {
      this.updateSale(cancelledSale);
      this.showHistory();
      this.showSuccess('Venda cancelada.');
    }, 'Não foi possível cancelar a venda.', true);
  }

  private requireEmployeeId(): number | null {
    const employeeId = this.auth.employeeId();

    if (employeeId === null) {
      this.notice.set({
        message: 'O funcionário da sessão não foi identificado. Entre novamente para continuar.',
        error: true,
      });
    }

    return employeeId;
  }

  private fetchSnapshot(): Observable<PdvSnapshot> {
    const selected = this.selectedSale();

    return forkJoin({
      cashes: this.cashApi.list(),
      sales: this.saleApi.list(),
      sale: selected ? this.saleApi.get(selected.id) : of(null),
    });
  }

  private applySnapshot(snapshot: PdvSnapshot): void {
    this.cashes.set(snapshot.cashes.sort((a, b) => b.id - a.id));
    this.sales.set(snapshot.sales.sort((a, b) => b.id - a.id));
    this.selectedSale.set(snapshot.sale);
    this.needsSync.set(false);
    this.loaded.set(true);
  }

  private run<T>(
    request: Observable<T>,
    applyResult: (data: T) => void,
    errorMessage: string,
    isWrite = false,
  ): void {
    if (this.busy()) return;

    this.busy.set(true);
    this.notice.set(null);

    request.pipe(
      tap(applyResult),
      catchError((error: unknown) => this.handleRequestError(error, errorMessage, isWrite)),
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.busy.set(false)),
    ).subscribe();
  }

  private handleRequestError(error: unknown, message: string, isWrite: boolean): Observable<unknown> {
    const responseUncertain = error instanceof HttpErrorResponse &&
      (error.status === 0 || error.status >= 500);

    if (isWrite && (responseUncertain || this.needsSync())) {
      return this.syncAfterWrite();
    }

    this.notice.set({ message: apiErrorMessage(error, message), error: true });
    return EMPTY;
  }

  private syncAfterWrite(): Observable<PdvSnapshot> {
    this.needsSync.set(true);
    this.cashAction.set(null);
    this.creatingSale.set(false);
    this.showSuccess('Conferindo o resultado da operação no servidor…');

    return this.fetchSnapshot().pipe(
      retry({ count: 2, delay: 500 }),
      tap(snapshot => {
        this.applySnapshot(snapshot);
        this.showSuccess('Dados atualizados. Confira o resultado antes de repetir a operação.');
      }),
      catchError(() => {
        this.notice.set({
          message: 'Não foi possível confirmar a operação. Reabra o PDV para conferir o resultado.',
          error: true,
        });
        return EMPTY;
      }),
    );
  }

  private updateSale(sale: SaleSummary): void {
    this.sales.update(sales => {
      const others = sales.filter(previous => previous.id !== sale.id);
      return [sale, ...others].sort((a, b) => b.id - a.id);
    });
  }

  private updateCash(cash: CashSession): void {
    this.cashes.update(cashes => {
      const others = cashes.filter(previous => previous.id !== cash.id);
      return [cash, ...others].sort((a, b) => b.id - a.id);
    });
  }

  private showSuccess(message: string): void {
    this.notice.set({ message, error: false });
  }
}
