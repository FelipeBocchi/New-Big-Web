import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface Funcionario {
  id: number;
  nome: string;
  cargo: string;
  telefone: string;
  status: 'Ativo' | 'Inativo';
}

@Component({
  selector: 'app-funcionarios-details',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './funcionarios-details.component.html',
  styleUrl: './funcionarios-details.component.scss'
})
export class FuncionariosDetailsComponent {

  @Input() modalAberto: boolean = false;

  @Input() modoEdicao: boolean = false;

  @Input() funcionario: Funcionario = {
    id: 0,
    nome: '',
    cargo: '',
    telefone: '',
    status: 'Ativo'
  };

  @Output() fechar = new EventEmitter<void>();

  @Output() salvarFuncionario =
    new EventEmitter<Funcionario>();

  fecharModal(): void {
    this.fechar.emit();
  }

  salvar(): void {
    this.salvarFuncionario.emit({
      ...this.funcionario
    });
  }

}