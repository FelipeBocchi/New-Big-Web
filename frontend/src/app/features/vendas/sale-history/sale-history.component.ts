import { CurrencyPipe } from '@angular/common';
import { Component, input, output } from '@angular/core';
import { PAYMENT_LABELS, SALE_STATUS_LABELS, SaleSummary } from '../sale.types';

@Component({
  selector: 'app-sales-history',
  imports: [CurrencyPipe],
  templateUrl: './sale-history.component.html',
  styleUrl: './sale-history.component.scss',
})
export class SalesHistoryComponent {
  readonly sales = input.required<readonly SaleSummary[]>();
  readonly loaded = input.required<boolean>();
  readonly busy = input.required<boolean>();

  readonly saleSelected = output<number>();

  readonly paymentLabels = PAYMENT_LABELS;
  readonly statusLabels = SALE_STATUS_LABELS;
}
