import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClientesDetailsComponent } from '../clientes-details/clientes-details.component';
import { ClienteService } from '../../../services/clientes/cliente.service';
import { Customer } from '../../../models/cutomers/customer';
import Swal from 'sweetalert2';

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
export class ClientesListComponent implements OnInit {

  termoBusca: string = '';
  clientes: Customer[] = [];
  editCliente: Customer = new Customer(0, "", "", "", "", false, new Date());

  clienteService = inject(ClienteService);

  modalAberto: boolean = false;
  modoEdicao: boolean = false;

  ngOnInit(): void {
    this.listAll();
  }

  listAll(): void {
    this.clienteService.listAll().subscribe({
      next: (lista) => {
        this.clientes = lista || [];
        this.verificarStateNavegacao();
      },
      error: (error) => {
        console.error('Erro ao listar clientes:', error);
        Swal.fire('Erro', 'Não foi possível carregar a lista de clientes.', 'error');
      }
    });
  }

  private verificarStateNavegacao(): void {
    const clienteNovo = history.state?.clienteNovo;
    const clienteEditado = history.state?.clienteEditado;

    if (clienteNovo) {
      this.clientes.push(clienteNovo);
    }

    if (clienteEditado) {
      const index = this.clientes.findIndex(x => x.id === clienteEditado.id);
      if (index !== -1) {
        this.clientes[index] = clienteEditado;
      }
    }
  }

  inativarById(id: number): void {
    Swal.fire({
      title: 'Tem certeza?',
      text: 'Deseja realmente inativar este cliente?',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sim, inativar!',
      cancelButtonText: 'Cancelar'
    }).then((result) => {
      
      if (result.isConfirmed) {
        this.clienteService.inativa(id).subscribe({
          next: () => {
            Swal.fire('Sucesso', 'Cliente inativado com sucesso.', 'success');
            this.listAll();
          },
          error: (err) => {
            console.error('Erro ao inativar:', err);
            Swal.fire('Erro', 'Erro ao inativar cliente.', 'error');
          }
        });
      }
    });
  }

  novoCliente(): void {
    this.modoEdicao = false;
    this.editCliente = new Customer(0, "", "", "", "", true, new Date());
    this.modalAberto = true;
  }

  editarCliente(cliente: Customer): void {
    this.modoEdicao = true;
    this.editCliente = { ...cliente };
    this.modalAberto = true;
  }

  fecharModal(): void {
    this.modalAberto = false;
  }

  retornoDetails(clienteSalvo: Customer): void {
    this.fecharModal();
    this.listAll();
  }

  alterarStatus(cliente: Customer): void {
    if (cliente) {
      cliente.active = !cliente.active;
      this.inativarById(cliente.id);
    }
  }

  get clientesFiltrados(): Customer[] {
    if (!this.clientes) return [];
    if (!this.termoBusca.trim()) return this.clientes;

    const busca = this.termoBusca.toLowerCase();
    return this.clientes.filter(cliente =>
      cliente.name?.toLowerCase().includes(busca) ||
      cliente.email?.toLowerCase().includes(busca)
    );
  }
}