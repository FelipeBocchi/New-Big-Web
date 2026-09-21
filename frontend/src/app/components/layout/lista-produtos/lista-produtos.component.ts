import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
// Ajuste o caminho do serviço de produtos se necessário
import { ProdutoService, Produto } from '../../../services/produtos/produto.service';

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

  // Signal para vincular ao input de busca do topo
  termoBusca = signal<string>('');

  // Objeto vinculado aos campos [(ngModel)] do modal
  novoProduto: Produto = { status: 'Ativo' } as Produto;

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
    this.novoProduto = { status: 'Ativo' } as Produto; // Reseta o form com o status padrão
    this.showModal = true;
  }

  fecharModal(): void {
    this.showModal = false;
  }

  salvarProduto(): void {
    this.produtoService.createProduct(this.novoProduto).subscribe({
      next: () => {
        alert('Produto cadastrado com sucesso!');
        this.fecharModal();
        this.carregarProdutos(); // Atualiza a tabela
      },
      error: (err) => {
        console.error('Erro ao salvar produto:', err);
        alert('Falha ao cadastrar produto.');
      }
    });
  }

excluirProduto(id?: string): void {
  if (!id) {
    console.error('ID do produto não foi encontrado.');
    return;
  }

  if (confirm('Tem certeza que deseja excluir este produto?')) {
    this.produtoService.deleteProduct(id).subscribe({
      next: () => {
        alert('Produto excluído com sucesso!');
        this.carregarProdutos();
      },
      error: (err) => {
        console.error('Erro ao excluir produto:', err);
        alert('Falha ao excluir produto.');
      }
    });
  }
}
}