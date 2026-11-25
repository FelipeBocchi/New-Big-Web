import { CurrencyPipe } from '@angular/common';
import { Component, input, OnChanges, output, signal, SimpleChanges } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  PAYMENT_LABELS,
  PaymentMethod,
  SALE_STATUS_LABELS,
  SaleDetails,
  SaleItemInput,
  SaleProduct,
} from '../sale.types';

@Component({
  selector: 'app-sale-editor',
  imports: [CurrencyPipe, ReactiveFormsModule],
  templateUrl: './sale-editor.component.html',
  styleUrl: './sale-editor.component.scss',
})
export class SaleEditorComponent implements OnChanges {
  readonly sale = input.required<SaleDetails>();
  readonly products = input.required<readonly SaleProduct[]>();
  readonly canEdit = input.required<boolean>();
  readonly disabled = input.required<boolean>();

  readonly back = output<void>();
  readonly itemAdded = output<SaleItemInput>();
  readonly paymentConfirmed = output<PaymentMethod>();
  readonly cancelRequested = output<void>();

  readonly statusLabels = SALE_STATUS_LABELS;
  readonly paymentLabels = PAYMENT_LABELS;
  readonly paymentMethods = Object.entries(PAYMENT_LABELS) as [PaymentMethod, string][];
  readonly confirmingCancel = signal(false);

  readonly itemForm = new FormGroup({
    barcode: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/\S/)],
    }),
    quantity: new FormControl<number | null>(1, [
      Validators.required,
      Validators.min(1),
      Validators.max(2_147_483_647),
      Validators.pattern(/^\d+$/),
    ]),
    discountUnit: new FormControl<number | null>(null, [
      Validators.min(0),
      Validators.max(9_999_999_999.99),
      ({ value }) => value === null || Number(value.toFixed(2)) === value
        ? null
        : { currencyPrecision: true },
    ]),
  });

  readonly paymentForm = new FormGroup({
    method: new FormControl<PaymentMethod | null>(null, Validators.required),
  });

  ngOnChanges(changes: SimpleChanges): void {
    const saleChange = changes['sale'];
    if (!saleChange) return;

    const previous = saleChange.previousValue as SaleDetails | undefined;
    const current = this.sale();
    const changedSale = previous?.id !== current.id;

    if (!previous || changedSale || previous.items.length !== current.items.length) {
      this.itemForm.reset({ quantity: 1 });
    }

    if (changedSale) {
      this.paymentForm.reset();
      this.confirmingCancel.set(false);
    }
  }

  addItem(): void {
    this.itemForm.markAllAsTouched();

    if (this.disabled() || !this.canEdit() || this.itemForm.invalid) return;

    const { barcode, quantity, discountUnit } = this.itemForm.getRawValue();
    if (quantity === null) return;

    this.itemAdded.emit({
      barcode: barcode.trim(),
      quantity,
      discountUnit: discountUnit ?? 0,
    });
  }

  finishSale(): void {
    this.paymentForm.markAllAsTouched();

    const method = this.paymentForm.controls.method.value;
    if (this.disabled() || !this.canEdit() || !method || this.sale().total <= 0) return;

    this.paymentConfirmed.emit(method);
  }
}
