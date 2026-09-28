import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Customer } from '../../models/cutomers/customer';

@Injectable({
  providedIn: 'root'
})
export class ClienteService {

  http = inject(HttpClient);

  API = "http://localhost:8081/api/customer";

  constructor() { }

  listAll(): Observable<Customer[]> {
    return this.http.get<Customer[]>(this.API);
  }

  save(cliente: Customer): Observable<Customer> {
    return this.http.post<Customer>(this.API, cliente);
  }

  inativa(id: number): Observable<any> {
    return this.http.delete<any>(this.API + "/" + id)
  }

  update(cliente: Customer, id: number): Observable<Customer> {
    return this.http.put<Customer>(this.API + "/" + id, cliente);
  }

  findById(id: number): Observable<Customer> {
    return this.http.get<Customer>(this.API + "/" + id);
  }

}
