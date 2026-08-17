import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class Login {

  constructor(private router: Router) {}

  login: string = '';
  senha: string = '';

  mostrarSenha: boolean = false;
  erro: string = '';
  sucesso: string = '';

  fazerLogin(): void {
    this.erro = '';
    this.sucesso = '';

    if (!this.login || !this.senha) {
      this.erro = 'Preencha o login e a senha.';
      return;
    }

    // Login provisório para testar a tela.
    // Depois vamos substituir pelo backend.
    if (this.login === 'login' && this.senha === '123456') {
      this.sucesso = 'Login realizado com sucesso!';
      this.router.navigate(['/funcionarios']);
    } else {
      this.erro = 'Login ou senha incorretos.';
    }
  }

  alternarSenha(): void {
    this.mostrarSenha = !this.mostrarSenha;
  }
}