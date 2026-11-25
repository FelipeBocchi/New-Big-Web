import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map } from 'rxjs';
import { API_BASE_URL } from '../../core/services/api';
import { PaymentMethod, SaleDetails, SaleItemInput, SaleProduct, SaleSummary } from './sale.types';

interface OpenSaleRequest {
  cashId: number;
  employeeId: number;
  customerId: number | null;
}

type OpenSaleResponse = Omit<SaleSummary, 'cashId' | 'paymentMethod'>;

@Injectable({ providedIn: 'root' })
export class SaleApiService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_BASE_URL}/sale`;

  list() {
    return this.http.get<SaleSummary[]>(this.url);
  }

  get(id: number) {
    return this.http.get<SaleDetails>(`${this.url}/${id}`);
  }

  products() {
    return this.http.get<SaleProduct[]>(`${API_BASE_URL}/product`).pipe(
      map(products => products.filter(product => product.active)),
    );
  }

  open(request: OpenSaleRequest) {
    return this.http.post<OpenSaleResponse>(this.url, request).pipe(
      map((sale): SaleDetails => ({ ...sale, cashId: request.cashId, paymentMethod: null, items: [] })),
    );
  }

  addItem(sale: SaleDetails, item: SaleItemInput) {
    return this.http.put(this.url, { id: sale.id, cashId: sale.cashId, ...item }, { responseType: 'text' });
  }

  close(sale: SaleDetails, paymentMethod: PaymentMethod) {
    return this.http.patch<SaleSummary>(`${this.url}/${sale.id}`, {
      cashId: sale.cashId,
      paymentMethod,
      valueArrived: sale.total,
    });
  }

  cancel(sale: SaleDetails) {
    return this.http.post<SaleSummary>(`${this.url}/${sale.id}/cancel`, { cashId: sale.cashId });
  }
}
