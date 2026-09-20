import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EstoqueDetailsComponent } from '../estoque-details/estoque-details.component';

interface Produto {
  id: number;
  nome: string;
  categoria: string;
  quantidade: number;
  valor: number;
  status: 'Ativo' | 'Inativo';
}

@Component({
  selector: 'app-estoque-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    EstoqueDetailsComponent
  ],
  templateUrl: './estoque-list.component.html',
  styleUrl: './estoque-list.component.scss'
})
export class EstoqueListComponent {

  termoBusca: string = '';

  modalAberto: boolean = false;
  modoEdicao: boolean = false;

  produtoSelecionado: Produto = {
    id: 0,
    nome: '',
    categoria: '',
    quantidade: 0,
    valor: 0,
    status: 'Ativo'
  };

  produtos: Produto[] = [
    {
      id: 1,
      nome: 'Sorvete de Chocolate',
      categoria: 'Sorvetes',
      quantidade: 30,
      valor: 12.00,
      status: 'Ativo'
    },
    {
      id: 2,
      nome: 'Sorvete de Morango',
      categoria: 'Sorvetes',
      quantidade: 25,
      valor: 12.00,
      status: 'Ativo'
    },
    {
      id: 3,
      nome: 'Sorvete de Baunilha',
      categoria: 'Sorvetes',
      quantidade: 20,
      valor: 10.00,
      status: 'Ativo'
    },
    {
      id: 4,
      nome: 'Sorvete de Flocos',
      categoria: 'Sorvetes',
      quantidade: 18,
      valor: 11.00,
      status: 'Ativo'
    },
    {
      id: 5,
      nome: 'Sorvete de Napolitano',
      categoria: 'Sorvetes',
      quantidade: 12,
      valor: 13.00,
      status: 'Ativo'
    },
    {
      id: 6,
      nome: 'Sorvete de Pistache',
      categoria: 'Sorvetes',
      quantidade: 0,
      valor: 15.00,
      status: 'Inativo'
    }
  ];


  // Busca somente pelo nome
  get produtosFiltrados(): Produto[] {
    if (!this.termoBusca.trim()) {
      return this.produtos;
    }

    const busca = this.termoBusca.toLowerCase();

    return this.produtos.filter(produto =>
      produto.nome.toLowerCase().includes(busca)
    );
  }


  // Abrir modal para cadastrar
  novoProduto(): void {
    this.modoEdicao = false;

    this.produtoSelecionado = {
      id: 0,
      nome: '',
      categoria: '',
      quantidade: 0,
      valor: 0,
      status: 'Ativo'
    };

    this.modalAberto = true;
  }


  // Abrir modal para editar
  editarProduto(produto: Produto): void {
    this.modoEdicao = true;

    this.produtoSelecionado = {
      ...produto
    };

    this.modalAberto = true;
  }


  // Fechar modal
  fecharModal(): void {
    this.modalAberto = false;
  }


  // Salvar produto
  salvarProduto(produto: Produto): void {

    if (this.modoEdicao) {

      const indice = this.produtos.findIndex(
        p => p.id === produto.id
      );

      if (indice !== -1) {
        this.produtos[indice] = {
          ...produto
        };
      }

    } else {

      const novoId = this.produtos.length > 0
        ? Math.max(...this.produtos.map(p => p.id)) + 1
        : 1;

      this.produtos.push({
        ...produto,
        id: novoId
      });

    }

    this.fecharModal();
  }


  // Alterar status
  alterarStatus(produto: Produto): void {
    produto.status =
      produto.status === 'Ativo'
        ? 'Inativo'
        : 'Ativo';
  }

}