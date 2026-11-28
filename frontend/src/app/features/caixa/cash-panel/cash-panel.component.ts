import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, input, output } from '@angular/core';
import { CashSession } from '../cash-api.service';

@Component({
  selector: 'app-cash-panel',
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './cash-panel.component.html',
  styleUrl: './cash-panel.component.scss',
})
export class CashPanelComponent {
  readonly cash = input.required<CashSession | null>();
  readonly openCashes = input.required<readonly CashSession[]>();
  readonly loaded = input.required<boolean>();
  readonly disabled = input.required<boolean>();
  readonly hasOpenSales = input.required<boolean>();

  readonly cashSelected = output<number>();
  readonly openRequested = output<void>();
  readonly closeRequested = output<void>();

  readonly isOpen = computed(() => this.cash()?.status === 'OPEN');
}
