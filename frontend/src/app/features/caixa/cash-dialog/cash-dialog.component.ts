import { CurrencyPipe } from '@angular/common';
import { AfterViewInit, Component, ElementRef, OnDestroy, ViewChild, computed, input, output } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CashSession } from '../cash-api.service';

export type CashAction = 'open' | 'close';

@Component({
  selector: 'app-cash-dialog',
  imports: [CurrencyPipe, ReactiveFormsModule],
  templateUrl: './cash-dialog.component.html',
  styleUrl: './cash-dialog.component.scss',
})
export class CashDialogComponent implements AfterViewInit, OnDestroy {
  @ViewChild('dialog', { static: true }) private dialog!: ElementRef<HTMLDialogElement>;

  readonly action = input.required<CashAction>();
  readonly cash = input.required<CashSession | null>();
  readonly saving = input.required<boolean>();
  readonly error = input('');
  readonly confirmed = output<number>();
  readonly dismissed = output<void>();

  readonly isOpening = computed(() => this.action() === 'open');
  readonly form = new FormGroup({
    amount: new FormControl<number | null>(null, [
      Validators.required,
      Validators.max(9_999_999_999.99),
      control => control.value === null ||
        (Number.isFinite(control.value) && Number(control.value.toFixed(2)) === control.value)
        ? null : { currencyPrecision: true },
    ]),
  });

  ngAfterViewInit(): void {
    const amount = this.form.controls.amount;
    amount.addValidators(Validators.min(this.isOpening() ? 0.01 : 0));
    amount.updateValueAndValidity();
    this.dialog.nativeElement.showModal();
  }

  ngOnDestroy(): void {
    this.dialog.nativeElement.close();
  }

  submit(): void {
    if (this.saving()) return;
    this.form.markAllAsTouched();
    const amount = this.form.controls.amount.value;
    if (this.form.valid && amount !== null) this.confirmed.emit(amount);
  }

  dismiss(event?: Event): void {
    event?.preventDefault();
    if (!this.saving()) this.dismissed.emit();
  }
}
