import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Employee } from '../../../models/employees/employee';
import { FuncionarioService } from '../../../services/funcionarios/funcionario.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-funcionarios-details',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './funcionarios-details.component.html',
  styleUrl: './funcionarios-details.component.scss'
})
export class FuncionariosDetailsComponent {

  @Input() funcionario: Employee = new Employee(0, "", "", "", "", "", "", true, true, new Date(), new Date(), "");
  @Input() modalAberto: boolean = false;
  @Input() modoEdicao: boolean = false;

  @Output() fechar = new EventEmitter<void>();
  @Output() salvarFuncionario = new EventEmitter<Employee>();

  funcionarioService = inject(FuncionarioService);

  save(): void {
    if (this.funcionario.id && this.funcionario.id > 0) {
      this.funcionarioService.update(this.funcionario, this.funcionario.id).subscribe({
        next: (retorno) => {
          Swal.fire('Sucesso', `${retorno.name || 'Funcionario'} atualizado com sucesso!`, 'success');
            this.salvarFuncionario.emit(retorno);
            this.fecharModal();
        },
        error: (error) => {
          console.error('Erro ao editar funcionario:', error);
          Swal.fire('Erro', 'Erro ao editar o funcionario.', 'error');
          this.fecharModal();
        }
      });
    } else {


      const funcionarioNovo = { ...this.funcionario};

      delete (funcionarioNovo as any).id;

      funcionarioNovo.cpf = funcionarioNovo.cpf.replace(/\D/g, '');

      if (funcionarioNovo.birthDate && funcionarioNovo.birthDate instanceof Date) {
        const ano = funcionarioNovo.birthDate.getFullYear();
        const mes = String(funcionarioNovo.birthDate.getMonth() + 1).padStart(2, '0');
        const dia = String(funcionarioNovo.birthDate.getDate()).padStart(2, '0');
        (funcionarioNovo as any).birthDate = `${ano}-${mes}-${dia}`;
      }

      console.log('JSON pronto para o Java:', funcionarioNovo);

      this.funcionarioService.save(funcionarioNovo as Employee).subscribe({
        next: (retorno) => {
          Swal.fire('Sucesso', 'Funcionario cadastrado com sucesso!', 'success');
          this.salvarFuncionario.emit(retorno);
          this.fecharModal();
        },
        error: (error) => {
          console.error('ERRO HTTP:', error);
          Swal.fire('Erro', 'Verifique os dados preenchidos.', 'error');
          this.fecharModal();
        }
      });
    }
  }

  fecharModal(): void {
    this.fechar.emit();
  }
}