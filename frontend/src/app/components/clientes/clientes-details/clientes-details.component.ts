import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Customer } from '../../../models/cutomers/customer';
import { ClienteService } from '../../../services/clientes/cliente.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-clientes-details',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './clientes-details.component.html',
  styleUrl: './clientes-details.component.scss'
})
export class ClientesDetailsComponent {

  @Input() cliente: Customer = new Customer(0, "", "", "", "", true, new Date());
  @Input() modalAberto: boolean = false;
  @Input() modoEdicao: boolean = false;

  @Output() fechar = new EventEmitter<void>();
  @Output() salvarCliente = new EventEmitter<Customer>();

  clienteService = inject(ClienteService);

  save(): void {
    if (this.cliente.id && this.cliente.id > 0) {
      this.clienteService.update(this.cliente, this.cliente.id).subscribe({
        next: (retorno) => {
          Swal.fire('Sucesso', `${retorno.name || 'Cliente'} atualizado com sucesso!`, 'success');
          this.salvarCliente.emit(retorno);
        },
        error: (error) => {
          console.error('Erro ao editar cliente:', error);
          Swal.fire('Erro', 'Erro ao editar o cliente.', 'error');
        }
      });
    } else {

      
      const clienteNovo = { ...this.cliente }; // Criamos uma cópia do cliente para enviar

      delete (clienteNovo as any).id; // Removemos o ID

      if (clienteNovo.birthDate && clienteNovo.birthDate instanceof Date) {  // Se a data estiver como um Objeto 'Date' do Javascript, convertemos para string "YYYY-MM-DD"
        const ano = clienteNovo.birthDate.getFullYear();
        const mes = String(clienteNovo.birthDate.getMonth() + 1).padStart(2, '0');
        const dia = String(clienteNovo.birthDate.getDate()).padStart(2, '0');
        (clienteNovo as any).birthDate = `${ano}-${mes}-${dia}`;
      }

      console.log('JSON pronto para o Java:', clienteNovo);

      this.clienteService.save(clienteNovo as Customer).subscribe({
        next: (retorno) => {
          Swal.fire('Sucesso', 'Cliente cadastrado com sucesso!', 'success');
          this.salvarCliente.emit(retorno);
        },
        error: (error) => {
          console.error('ERRO HTTP:', error);
          Swal.fire('Erro', 'Verifique os dados preenchidos.', 'error');
        }
      });
    }
  }

  fecharModal(): void {
    this.fechar.emit();
  }
}