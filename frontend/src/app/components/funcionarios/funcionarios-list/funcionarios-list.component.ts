import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FuncionariosDetailsComponent } from '../funcionarios-details/funcionarios-details.component';
import { FuncionarioService } from '../../../services/funcionarios/funcionario.service';
import { Employee } from '../../../models/employees/employee';
import Swal, { SweetAlertResult } from 'sweetalert2';


@Component({
  selector: 'app-funcionarios-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    FuncionariosDetailsComponent
  ],
  templateUrl: './funcionarios-list.component.html',
  styleUrl: './funcionarios-list.component.scss'
})
export class FuncionariosListComponent implements OnInit {

  termoBusca: string = '';
  funcionarios: Employee[] = [];
  editFuncionario: Employee = new Employee(0, "", "", "", "", "", "", false, false, new Date(), new Date(), "");

  funcionarioService = inject(FuncionarioService);

  modalAberto: boolean = false;
  modoEdicao: boolean = false;

  ngOnInit(): void {
    this.listAll();
  }

  listAll(): void {
    this.funcionarioService.listAll().subscribe({
      next: (lista) => {
        this.funcionarios = lista || [];
        this.verificarStateNavegacao();
      },
      error: (error) => {
        console.error('Erro ao listar funcionarios:', error);
        Swal.fire('Erro', 'Não foi possível carregar a lista de funcionários.', 'error');
      }
    });
  }

  private verificarStateNavegacao(): void {
    const funcionarioNovo = history.state?.funcionarioNovo;
    const funcionarioEditado = history.state?.funcionarioEditado;

    if (funcionarioNovo) {
      this.funcionarios.push(funcionarioNovo);
    }

    if (funcionarioEditado) {
      const index = this.funcionarios.findIndex(x => x.id === funcionarioEditado.id);
      if (index !== -1) {
        this.funcionarios[index] = funcionarioEditado;
      }
    }
  }

  alterarStatus(funcionario: Employee): void {
  const novoStatus = !funcionario.active;
  const acao = novoStatus ? 'ativar' : 'inativar';

  Swal.fire({
    title: 'Tem certeza?',
    text: `Deseja realmente ${acao} este funcionário?`,
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: `Sim, ${acao}!`,
    cancelButtonText: 'Cancelar'
  }).then((result: SweetAlertResult) => {

    if (result.isConfirmed) {
      this.funcionarioService.alterarStatus(
        funcionario.id,
        novoStatus
      ).subscribe({
        next: () => {
          Swal.fire(
            'Sucesso',
            `Funcionário ${novoStatus ? 'ativado' : 'inativado'} com sucesso!`,
            'success'
          );

          this.listAll();
        },
        error: (error) => {
          console.error('Erro ao alterar status:', error);
          Swal.fire('Erro', 'Não foi possível alterar o status.', 'error');
        }
      });
    }
  });
}

  novoFuncionario(): void {
    this.modoEdicao = false;
    this.editFuncionario = new Employee(0, "", "", "", "", "", "", true, false, new Date(), new Date(), "");
    this.modalAberto = true;
  }

  editarFuncionario(funcionario: Employee): void {
    this.modoEdicao = true;
    this.editFuncionario = { ...funcionario};
    this.modalAberto = true;
  }

  fecharModal(): void {
    this.modalAberto = false;
  }

  retornoDetails(funcionarioSalvo: Employee): void {
    this.fecharModal();
    this.listAll();
  }

  getCargoLabel(role: String): string {
  const cargos: Record<string, string> = {
    ATTENDANT: 'Atendente',
    CASHIER: 'Caixa',
    STOCK_CLERK: 'Estoquista',
    MANAGER: 'Gerente'
  };

  return cargos[String(role)] || String(role);
}

  get funcionariosFiltrados(): Employee[] {
    if(!this.funcionarios) return [];
    if (!this.termoBusca.trim()) return this.funcionarios;

    const busca = this.termoBusca.toLowerCase();
    return this.funcionarios.filter(funcionario =>
      funcionario.name?.toLowerCase().includes(busca) ||
      funcionario.email?.toLowerCase().includes(busca)
    );
  }
}