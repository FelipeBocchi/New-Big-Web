import { Component, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MenuComponent } from '../menu/menu.component';

export interface Produto {
  id: number;
  nome: string;
  codigoBarras: string;
  categoria: string;
  preco: number;
  status: 'Ativo' | 'Inativo';
}

@Component({
  selector: 'app-produto',
  standalone: true,
  imports: [CommonModule, FormsModule, MenuComponent],
  templateUrl: './produto.component.html',
  styleUrl: './produto.component.scss'
})
export class ProdutoComponent {
  termoBusca = signal('');
  paginaAtual = signal(1);

  produtos = signal<Produto[]>([
    { id: 1, nome: 'Sorvete de banana', codigoBarras: '1234 5670', categoria: 'Sorvete', preco: 8.50, status: 'Ativo' },
    { id: 2, nome: 'Sorvete de chocolate', codigoBarras: '2001 2345', categoria: 'Sorvete', preco: 10.50, status: 'Ativo' },
    { id: 3, nome: 'Açaí 1L', codigoBarras: '3004 5678', categoria: 'Açaí', preco: 30.00, status: 'Inativo' },
    { id: 4, nome: 'Napolitando 1L', codigoBarras: '4007 8901', categoria: 'Sorvete', preco: 28.00, status: 'Inativo' },
    { id: 5, nome: 'Sorvete casquinha', codigoBarras: '5002 4684', categoria: 'Sorvete', preco: 5.00, status: 'Ativo' },
    { id: 6, nome: 'Picole ao leite', codigoBarras: '6001 3579', categoria: 'Picole', preco: 2.50, status: 'Ativo' },
    { id: 7, nome: 'Milksheik 330ml', codigoBarras: '7009 8765', categoria: 'Bebida', preco: 15.00, status: 'Ativo' },
  ]);

  produtosFiltrados = computed(() => {
    const termo = this.termoBusca().toLowerCase().trim();
    if (!termo) return this.produtos();
    return this.produtos().filter(p =>
      p.nome.toLowerCase().includes(termo) ||
      p.codigoBarras.includes(termo) ||
      p.categoria.toLowerCase().includes(termo)
    );
  });

  eliminarProduto(id: number) {
    if (confirm('Tens a certeza que desejas eliminar este produto?')) {
      this.produtos.update(lista => lista.filter(p => p.id !== id));
    }
  }
}