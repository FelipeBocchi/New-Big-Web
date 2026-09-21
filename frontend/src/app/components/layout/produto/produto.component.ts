import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MenuComponent } from '../menu/menu.component';
import { RouterOutlet } from '@angular/router';
import { ProdutoService, Produto } from '../../../services/produtos/produto.service';

@Component({
  selector: 'app-produto',
  imports: [
    CommonModule,  // Necessário para *ngIf, *ngFor e pipes
    FormsModule,   // Necessário para [(ngModel)] nos inputs
    MenuComponent, 
    RouterOutlet
  ],
  templateUrl: './produto.component.html',
  styleUrl: './produto.component.scss'
})
export class ProdutoComponent implements OnInit {

  produtos: Produto[] = [];
  showModal: boolean = false;

  // Signal para controlar o termo da busca
  termoBusca = signal<string>('');

  // Objeto para vincular ao formulário do modal
  novoProduto: Produto = { status: 'Ativo' } as Produto;

  constructor(private produtoService: ProdutoService) {}

  ngOnInit(): void {
    this.carregarProdutos();
  }

  // Filtra a lista de produtos em tempo real com base no termo buscado
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
    this.novoProduto = { status: 'Ativo' } as Produto; // Limpa o formulário e define status padrão
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
        this.carregarProdutos(); // Recarrega a lista atualizada
      },
      error: (err) => {
        console.error('Erro ao salvar produto:', err);
        alert('Falha ao cadastrar produto.');
      }
    });
  }

  excluirProduto(id: string): void {
    if (confirm('Tem certeza que deseja excluir este produto?')) {
      this.produtoService.deleteProduct(id).subscribe({
        next: () => {
          alert('Produto excluído com sucesso!');
          this.carregarProdutos(); // Recarrega a lista atualizada
        },
        error: (err) => {
          console.error('Erro ao excluir produto:', err);
          alert('Falha ao excluir o produto.');
        }
      });
    }
  }
}