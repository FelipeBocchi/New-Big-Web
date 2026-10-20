import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// Interface ajustada conforme os DTOs do teu Spring Boot
export interface Produto {
  id?: string;
  name: string;
  description?: string;
  barCode?: string;
  category?: string;
  salePrice: number;
  costPrice?: number;
  status?: 'Ativo' | 'Inativo';
}

@Injectable({
  providedIn: 'root'
})
export class ProdutoService {
  private http = inject(HttpClient);
  
  // Endpoint do teu ProductController (ajuste a URL se for diferente)
  private apiUrl = 'http://localhost:8081/products'; 

  getAllProducts(): Observable<Produto[]> {
    return this.http.get<Produto[]>(this.apiUrl);
  }

  createProduct(produto: Produto): Observable<void> {
    return this.http.post<void>(this.apiUrl, produto);
  }

  updateProduct(id: string, produto: Produto): Observable<Produto> {
    return this.http.put<Produto>(`${this.apiUrl}/${id}`, produto);
  }

  deleteProduct(id: string): Observable<boolean> {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }
}