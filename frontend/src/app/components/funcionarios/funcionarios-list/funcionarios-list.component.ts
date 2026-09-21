import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FuncionariosDetailsComponent } from '../funcionarios-details/funcionarios-details.component';

interface Funcionario {
  id: number;
  nome: string;
  cargo: string;
  telefone: string;
  status: 'Ativo' | 'Inativo';
}

@Component({
  selector: 'app-funcionarios-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    FuncionariosDetailsComponent
  ],
  templateUrl: './funcionarios-list.component.html',
  styleUrl: './funcionarios-list.component.scss'
})
export class FuncionariosListComponent {

  termoBusca: string = '';

  // Controle do modal
  modalAberto: boolean = false;
  modoEdicao: boolean = false;

  funcionarioSelecionado: Funcionario = {
    id: 0,
    nome: '',
    cargo: '',
    telefone: '',
    status: 'Ativo'
  };

  funcionarios: Funcionario[] = [
    {
      id: 1,
      nome: 'João',
      cargo: 'Gerente',
      telefone: '(45) 99999-1111',
      status: 'Ativo'
    },
    {
      id: 2,
      nome: 'Roberto',
      cargo: 'Funcionário',
      telefone: '(45) 99999-2222',
      status: 'Ativo'
    },
    {
      id: 3,
      nome: 'Felipe',
      cargo: 'Funcionário',
      telefone: '(45) 99999-3333',
      status: 'Ativo'
    },
    {
      id: 4,
      nome: 'Gustavo',
      cargo: 'Funcionário',
      telefone: '(45) 99999-4444',
      status: 'Ativo'
    },
    {
      id: 5,
      nome: 'Luis',
      cargo: 'Funcionário',
      telefone: '(45) 99999-5555',
      status: 'Ativo'
    },
    {
      id: 6,
      nome: 'Neymar',
      cargo: 'Funcionário',
      telefone: '(45) 99999-6666',
      status: 'Inativo'
    }
  ];

  get funcionariosFiltrados(): Funcionario[] {

    if (!this.termoBusca.trim()) {
      return this.funcionarios;
    }

    const busca = this.termoBusca.toLowerCase();

    return this.funcionarios.filter(funcionario =>
      funcionario.nome.toLowerCase().includes(busca)
    );
  }

  // Abre o modal para criar
  novoFuncionario(): void {

    this.modoEdicao = false;

    this.funcionarioSelecionado = {
      id: 0,
      nome: '',
      cargo: '',
      telefone: '',
      status: 'Ativo'
    };

    this.modalAberto = true;
  }

  // Abre o modal para editar
  editarFuncionario(funcionario: Funcionario): void {

    this.modoEdicao = true;

    this.funcionarioSelecionado = {
      ...funcionario
    };

    this.modalAberto = true;
  }

  // Fecha o modal
  fecharModal(): void {
    this.modalAberto = false;
  }

  // Salva o funcionário
  salvarFuncionario(funcionario: Funcionario): void {

    if (this.modoEdicao) {

      const indice = this.funcionarios.findIndex(
        f => f.id === funcionario.id
      );

      if (indice !== -1) {
        this.funcionarios[indice] = {
          ...funcionario
        };
      }

    } else {

      const novoId = this.funcionarios.length > 0
        ? Math.max(...this.funcionarios.map(f => f.id)) + 1
        : 1;

      this.funcionarios.push({
        ...funcionario,
        id: novoId
      });

    }

    this.fecharModal();
  }

  // Altera o status
  alterarStatus(funcionario: Funcionario): void {

    funcionario.status =
      funcionario.status === 'Ativo'
        ? 'Inativo'
        : 'Ativo';

  }

}