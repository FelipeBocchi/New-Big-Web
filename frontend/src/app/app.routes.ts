import { Routes } from '@angular/router';
import { LoginComponent } from './components/layout/login/login.component';
import { PrincipalComponent } from './components/layout/principal/principal.component';
import { CashMasterComponent } from './components/caixa/cash-master/cash-master.component';
import { FuncionariosListComponent } from './components/funcionarios/funcionarios-list/funcionarios-list.component';
import { ClientesListComponent } from './components/clientes/clientes-list/clientes-list.component';
import { EstoqueListComponent } from './components/estoque/estoque-list/estoque-list.component';
import { ProdutoComponent } from './components/layout/produto/produto.component';
import { ListaProdutosComponent } from './components/layout/lista-produtos/lista-produtos.component';

export const routes: Routes = [

    { path: "", redirectTo: "login", pathMatch: "full" },
    { path: "login", component: LoginComponent },
    {
        path: "newBig", component: PrincipalComponent, children: [
            { path: "cash", component: CashMasterComponent },
            { path: "funcionarios", component: FuncionariosListComponent },
            { path: "clientes", component: ClientesListComponent },
            { path: "estoque", component: EstoqueListComponent },
            {
                path: "produto", component: ProdutoComponent, children: [
                    { path: '', component: ListaProdutosComponent } // Renderiza automaticamente dentro do <router-outlet>
                ]
            },
            //aqui vai entrar os componentes de telas que tenham o menu lateral e topos os componentes em principal
        ]
    }

];