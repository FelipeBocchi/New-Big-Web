import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// Interface ajustada conforme os DTOs do teu Spring Boot
export interface Produto {
  id?: string;             // String conforme teu repository.findById(String id)
  name: string;            // 'name' conforme teu ProductRequestDTO
  barCode?: string;        // ajuste conforme a propriedade no teu ProductResponseDTO
  category?: string;       // ajuste conforme a propriedade no teu ProductResponseDTO
  salePrice: number;       // 'salePrice' conforme teu ProductRequestDTO
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