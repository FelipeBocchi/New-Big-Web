import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface Cliente {
  id: number;
  nome: string;
  email: string;
  telefone: string;
  status: 'Ativo' | 'Inativo';
}

@Component({
  selector: 'app-clientes-details',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './clientes-details.component.html',
  styleUrl: './clientes-details.component.scss'
})
export class ClientesDetailsComponent {

  @Input() modalAberto: boolean = false;

  @Input() modoEdicao: boolean = false;

  @Input() cliente: Cliente = {
    id: 0,
    nome: '',
    email: '',
    telefone: '',
    status: 'Ativo'
  };

  @Output() fechar = new EventEmitter<void>();

  @Output() salvarCliente =
    new EventEmitter<Cliente>();

  fecharModal(): void {
    this.fechar.emit();
  }

  salvar(): void {
    this.salvarCliente.emit({
      ...this.cliente
    });
  }
}