export type SaleStatus = 'OPENED' | 'COMPLETED' | 'CANCELLED';
export type PaymentMethod = 'CASH' | 'CREDIT_CARD' | 'DEBIT_CARD' | 'PIX';

export const SALE_STATUS_LABELS: Record<SaleStatus, string> = {
  OPENED: 'Aberta',
  COMPLETED: 'Concluída',
  CANCELLED: 'Cancelada',
};

export const PAYMENT_LABELS: Record<PaymentMethod, string> = {
  CASH: 'Dinheiro',
  PIX: 'Pix',
  DEBIT_CARD: 'Cartão de débito',
  CREDIT_CARD: 'Cartão de crédito',
};

export interface SaleSummary {
  id: number;
  cashId: number;
  subtotal: number;
  discount: number;
  total: number;
  status: SaleStatus;
  paymentMethod: PaymentMethod | null;
  customerName: string | null;
  employeeName: string;
}

export interface SaleDetails extends SaleSummary {
  items: SaleItem[];
}

export interface SaleItem {
  id: number;
  productName: string;
  unitPrice: number;
  quantity: number;
  total: number;
}

export interface SaleItemInput {
  barcode: string;
  quantity: number;
  discountUnit: number;
}

export interface SaleProduct {
  barcode: string;
  name: string;
  active: boolean;
}
