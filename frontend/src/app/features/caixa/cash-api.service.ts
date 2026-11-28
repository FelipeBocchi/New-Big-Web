import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map } from 'rxjs';
import { API_BASE_URL } from '../../core/services/api';

export interface CashSession {
  id: number;
  status: 'OPEN' | 'CLOSED';
  openingAmount: number;
  closingAmount: number | null;
  expectedClosingAmount: number;
  total: number;
  openedAt: string;
  closedAt: string | null;
  openedBy: string;
  closedBy: string | null;
}

interface OpenCashRequest {
  openingAmount: number;
  employeeId: number;
}

interface CloseCashRequest {
  closingAmount: number;
  employeeId: number;
}

interface OpenCashResponse {
  id: number;
  status: 'OPEN';
  openingAmount: number;
  total: number;
  openedAt: string;
  nameOfEmployee: string;
}

@Injectable({ providedIn: 'root' })
export class CashApiService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_BASE_URL}/cash`;

  list() {
    return this.http.get<CashSession[]>(this.url);
  }

  get(id: number) {
    return this.http.get<CashSession>(`${this.url}/${id}`);
  }

  open(request: OpenCashRequest) {
    return this.http.post<OpenCashResponse>(this.url, request).pipe(
      map(({ id, status, openingAmount, total, openedAt, nameOfEmployee }): CashSession => ({
        id,
        status,
        openingAmount,
        total,
        openedAt,
        openedBy: nameOfEmployee,
        expectedClosingAmount: openingAmount,
        closingAmount: null,
        closedAt: null,
        closedBy: null,
      })),
    );
  }

  close(id: number, request: CloseCashRequest) {
    return this.http.patch<CashSession>(`${this.url}/${id}`, request);
  }
}