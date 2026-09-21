import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  private http = inject(HttpClient);

  // Variáveis para guardar os dados digitados
  email = '';
  password = '';

  onSubmit() {
    // Faz a chamada HTTP que será interceptada pelo mockAuthInterceptor
    this.http.post('/api/login', { email: this.email, password: this.password }).subscribe({
      next: (response) => {
        console.log('Login com sucesso:', response);
        alert('Login realizado com sucesso!');
      },
      error: (err) => {
        console.error('Erro no login:', err);
        alert(err.error?.message || 'Erro ao realizar login');
      }
    });
  }
}