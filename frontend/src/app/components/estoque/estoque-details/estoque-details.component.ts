import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface Produto {
  id: number;
  nome: string;
  categoria: string;
  quantidade: number;
  valor: number;
  status: 'Ativo' | 'Inativo';
}

@Component({
  selector: 'app-estoque-details',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './estoque-details.component.html',
  styleUrl: './estoque-details.component.scss'
})
export class EstoqueDetailsComponent {

  @Input() modalAberto: boolean = false;

  @Input() modoEdicao: boolean = false;

  @Input() produto: Produto = {
    id: 0,
    nome: '',
    categoria: '',
    quantidade: 0,
    valor: 0,
    status: 'Ativo'
  };

  @Output() fechar = new EventEmitter<void>();

  @Output() salvarProduto = new EventEmitter<Produto>();


  fecharModal(): void {
    this.fechar.emit();
  }


  salvar(): void {
    this.salvarProduto.emit({ ...this.produto });
  }

}