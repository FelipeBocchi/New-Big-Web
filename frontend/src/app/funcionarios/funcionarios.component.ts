import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface Funcionario {
  id: number;
  nome: string;
  cargo: string;
  telefone: string;
  status: string;
}

@Component({
  selector: 'app-funcionarios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './funcionarios.component.html',
  styleUrl: './funcionarios.component.scss'
})
export class FuncionariosComponent {

  funcionarios: Funcionario[] = [
    {
      id: 1,
      nome: 'João',
      cargo: '******',
      telefone: '(45) 99999-1111',
      status: 'Ativo'
    },
    {
      id: 2,
      nome: 'Maria',
      cargo: '******',
      telefone: '(45) 99999-2222',
      status: 'Ativo'
    },
    {
      id: 3,
      nome: 'Carlos',
      cargo: '******',
      telefone: '(45) 99999-3333',
      status: 'Ativo'
    },
    {
      id: 4,
      nome: 'Ana',
      cargo: '******',
      telefone: '(45) 99999-4444',
      status: 'Inativo'
    }
  ];

  busca: string = '';

  // Controla o modal
  modalAberto: boolean = false;

  // Define o tipo de operação
  modoModal: 'criar' | 'editar' | 'excluir' = 'criar';

  funcionarioSelecionado: Funcionario | null = null;

  novoFuncionario: Funcionario = this.criarFuncionarioVazio();


  get funcionariosFiltrados(): Funcionario[] {

    const termo = this.busca.toLowerCase().trim();

    if (!termo) {
      return this.funcionarios;
    }

    return this.funcionarios.filter(funcionario =>
      funcionario.nome.toLowerCase().includes(termo)
    );
  }

  abrirModalCriar(): void {

    this.modoModal = 'criar';

    this.funcionarioSelecionado = null;

    this.novoFuncionario = this.criarFuncionarioVazio();

    this.modalAberto = true;
  }

  abrirModalEditar(funcionario: Funcionario): void {

    this.modoModal = 'editar';

    this.funcionarioSelecionado = funcionario;

    this.novoFuncionario = {
      ...funcionario
    };

    this.modalAberto = true;
  }

  abrirModalExcluir(funcionario: Funcionario): void {

    this.modoModal = 'excluir';

    this.funcionarioSelecionado = funcionario;

    this.modalAberto = true;
  }

  salvar(): void {

    if (
      !this.novoFuncionario.nome.trim() ||
      !this.novoFuncionario.cargo.trim() ||
      !this.novoFuncionario.telefone.trim()
    ) {
      alert('Preencha todos os campos obrigatórios.');
      return;
    }


    if (this.modoModal === 'criar') {

      const novoId = this.funcionarios.length > 0
        ? Math.max(...this.funcionarios.map(f => f.id)) + 1
        : 1;

      this.novoFuncionario.id = novoId;

      this.funcionarios.push({
        ...this.novoFuncionario
      });
    }


    if (
      this.modoModal === 'editar' &&
      this.funcionarioSelecionado
    ) {

      const indice = this.funcionarios.findIndex(
        funcionario =>
          funcionario.id === this.funcionarioSelecionado!.id
      );

      if (indice !== -1) {

        this.funcionarios[indice] = {
          ...this.novoFuncionario
        };

      }
    }

    this.fecharModal();
  }

  confirmarExclusao(): void {

    if (!this.funcionarioSelecionado) {
      return;
    }

    this.funcionarios = this.funcionarios.filter(
      funcionario =>
        funcionario.id !== this.funcionarioSelecionado!.id
    );

    this.fecharModal();
  }

  fecharModal(): void {

    this.modalAberto = false;

    this.funcionarioSelecionado = null;

    this.novoFuncionario = this.criarFuncionarioVazio();
  }

  private criarFuncionarioVazio(): Funcionario {

    return {
      id: 0,
      nome: '',
      cargo: '',
      telefone: '',
      status: 'Ativo'
    };
  }
}