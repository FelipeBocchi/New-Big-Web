import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClientesDetailsComponent } from '../clientes-details/clientes-details.component';

interface Cliente {
  id: number;
  nome: string;
  email: string;
  telefone: string;
  status: 'Ativo' | 'Inativo';
}

@Component({
  selector: 'app-clientes-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ClientesDetailsComponent
  ],
  templateUrl: './clientes-list.component.html',
  styleUrl: './clientes-list.component.scss'
})
export class ClientesListComponent {

  termoBusca: string = '';

  // Controle do modal
  modalAberto: boolean = false;
  modoEdicao: boolean = false;

  clienteSelecionado: Cliente = {
    id: 0,
    nome: '',
    email: '',
    telefone: '',
    status: 'Ativo'
  };

  clientes: Cliente[] = [
    {
      id: 1,
      nome: 'João',
      email: 'joaocliente@gmail.com',
      telefone: '(45) 99999-1111',
      status: 'Ativo'
    },
    {
      id: 2,
      nome: 'Roberto',
      email: 'robertocliente@gmail.com',
      telefone: '(45) 99999-2222',
      status: 'Ativo'
    },
    {
      id: 3,
      nome: 'Felipe',
      email: 'felipecliente@gmail.com',
      telefone: '(45) 99999-3333',
      status: 'Ativo'
    },
    {
      id: 4,
      nome: 'Gustavo',
      email: 'gustavocliente@gmail.com',
      telefone: '(45) 99999-4444',
      status: 'Ativo'
    },
    {
      id: 5,
      nome: 'Luis',
      email: 'luiscliente@gmail.com',
      telefone: '(45) 99999-5555',
      status: 'Ativo'
    },
    {
      id: 6,
      nome: 'Neymar',
      email: 'neymarcliente@gmail.com',
      telefone: '(45) 99999-6666',
      status: 'Inativo'
    }
  ];

  get clientesFiltrados(): Cliente[] {

    if (!this.termoBusca.trim()) {
      return this.clientes;
    }

    const busca = this.termoBusca.toLowerCase();

    return this.clientes.filter(cliente =>
      cliente.nome.toLowerCase().includes(busca)
    );
  }

  // Abrir modal para novo cliente
  novoCliente(): void {

    this.modoEdicao = false;

    this.clienteSelecionado = {
      id: 0,
      nome: '',
      email: '',
      telefone: '',
      status: 'Ativo'
    };

    this.modalAberto = true;

    console.log('Modal:', this.modalAberto);
  }

  // Abrir modal para editar
  editarCliente(cliente: Cliente): void {

    this.modoEdicao = true;

    this.clienteSelecionado = {
      ...cliente
    };

    this.modalAberto = true;

    console.log('Modal:', this.modalAberto);
  }

  // Fechar modal
  fecharModal(): void {
    this.modalAberto = false;
  }

  // Salvar cliente
  salvarCliente(cliente: Cliente): void {

    if (this.modoEdicao) {

      const indice = this.clientes.findIndex(
        c => c.id === cliente.id
      );

      if (indice !== -1) {
        this.clientes[indice] = {
          ...cliente
        };
      }

    } else {

      const novoId = this.clientes.length > 0
        ? Math.max(...this.clientes.map(c => c.id)) + 1
        : 1;

      this.clientes.push({
        ...cliente,
        id: novoId
      });
    }

    this.fecharModal();
  }

  // Alterar status
  alterarStatus(cliente: Cliente): void {

    cliente.status =
      cliente.status === 'Ativo'
        ? 'Inativo'
        : 'Ativo';

  }

}