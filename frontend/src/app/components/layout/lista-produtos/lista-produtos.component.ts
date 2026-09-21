import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
// Ajuste o caminho do serviço de produtos se necessário
import { ProdutoService, Produto } from '../../../services/produto.service';

@Component({
  selector: 'app-lista-produtos',
  standalone: true,
  imports: [
    CommonModule,  // Necessário para *ngIf, *ngFor e ngClass
    FormsModule    // Necessário para [(ngModel)] nos inputs do modal
  ],
  templateUrl: './lista-produtos.component.html',
  styleUrl: './lista-produtos.component.scss'
})
export class ListaProdutosComponent implements OnInit {

  produtos: Produto[] = [];
  showModal: boolean = false;
  showDeleteModal: boolean = false;
  modoEdicao: boolean = false;
  produtoParaExcluir: Produto | null = null;

  // Signal para vincular ao input de busca do topo
  termoBusca = signal<string>('');

  // Objeto vinculado aos campos [(ngModel)] do modal
  novoProduto: Produto = {
    name: '',
    description: '',
    salePrice: 0,
    costPrice: 0,
    status: 'Ativo'
  };

  constructor(private produtoService: ProdutoService) {}

  ngOnInit(): void {
    this.carregarProdutos();
  }

  // Filtra os produtos em tempo real com base no que for digitado na busca
  produtosFiltrados = computed(() => {
    const termo = this.termoBusca().toLowerCase().trim();
    if (!termo) {
      return this.produtos;
    }
    return this.produtos.filter(p =>
      p.name?.toLowerCase().includes(termo) ||
      p.barCode?.toLowerCase().includes(termo) ||
      p.category?.toLowerCase().includes(termo)
    );
  });

  carregarProdutos(): void {
    this.produtoService.getAllProducts().subscribe({
      next: (dados) => this.produtos = dados,
      error: (err) => console.error('Erro ao carregar produtos:', err)
    });
  }

  abrirModal(): void {
    this.modoEdicao = false;
    this.novoProduto = {
      name: '',
      description: '',
      salePrice: 0,
      costPrice: 0,
      status: 'Ativo'
    };
    this.showModal = true;
  }

  abrirModalEdicao(produto: Produto): void {
    this.modoEdicao = true;
    this.novoProduto = { ...produto };
    this.showModal = true;
  }

  abrirModalExclusao(produto: Produto): void {
    this.produtoParaExcluir = produto;
    this.showDeleteModal = true;
  }

  fecharModal(): void {
    this.showModal = false;
    this.modoEdicao = false;
  }

  fecharModalExclusao(): void {
    this.showDeleteModal = false;
    this.produtoParaExcluir = null;
  }

  salvarProduto(): void {
    if (this.modoEdicao && this.novoProduto.id) {
      this.produtoService.updateProduct(this.novoProduto.id, this.novoProduto).subscribe({
        next: () => {
          alert('Produto atualizado com sucesso!');
          this.fecharModal();
          this.carregarProdutos();
        },
        error: (err: unknown) => {
          console.error('Erro ao atualizar produto:', err);
          alert('Falha ao atualizar o produto.');
        }
      });
      return;
    }

    this.produtoService.createProduct(this.novoProduto).subscribe({
      next: () => {
        alert('Produto cadastrado com sucesso!');
        this.fecharModal();
        this.carregarProdutos();
      },
      error: (err: unknown) => {
        console.error('Erro ao salvar produto:', err);
        alert('Falha ao cadastrar produto.');
      }
    });
  }

  confirmarExclusao(): void {
    if (!this.produtoParaExcluir?.id) {
      console.error('ID do produto não foi encontrado.');
      this.fecharModalExclusao();
      return;
    }

    this.produtoService.deleteProduct(this.produtoParaExcluir.id).subscribe({
      next: () => {
        alert('Produto excluído com sucesso!');
        this.fecharModalExclusao();
        this.carregarProdutos();
      },
      error: (err: unknown) => {
        console.error('Erro ao excluir produto:', err);
        alert('Falha ao excluir produto.');
      }
    });
  }

  excluirProduto(id?: string): void {
    const produto = this.produtos.find(p => p.id === id);
    if (!produto) {
      console.error('Produto não encontrado para exclusão.');
      return;
    }

    this.abrirModalExclusao(produto);
  }
}