import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Employee } from '../../models/employees/employee';

@Injectable({
  providedIn: 'root'
})
export class FuncionarioService {

  http = inject(HttpClient);

  API = "http://localhost:8081/api/employee";

  constructor() { }

  listAll(): Observable<Employee[]> {
    return this.http.get<Employee[]>(this.API);
  }

  save(funcionario: Employee): Observable<Employee> {
    return this.http.post<Employee>(this.API, funcionario);
  }

  alterarStatus(id: number, active: boolean): Observable<any> {
  return this.http.patch<any>(
    `${this.API}/${id}`,
    { active }
    );
  }

  ativa(funcionario: Employee, id: number): Observable<Employee> {
    return this.http.patch<Employee>(this.API + "/" + id, funcionario);
  }

  update(funcionario: Employee, id: number): Observable<Employee> {
    return this.http.put<Employee>(this.API + "/" + id, funcionario);
  }

  findById(id: number): Observable<Employee> {
    return this.http.get<Employee>(this.API + "/" + id); 
  }
  
}
