
import { Routes } from '@angular/router';
import { LoginComponent } from './components/layout/login/login.component';
import { PrincipalComponent } from './components/layout/principal/principal.component';
import { sessionGuard } from './core/guards/session.guards';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  {
    path: 'newBig',
    component: PrincipalComponent,
    canActivate: [sessionGuard],
    canActivateChild: [sessionGuard],
    children: [
      { path: '', redirectTo: 'cash', pathMatch: 'full' },
      { path: 'cash', loadComponent: () => import('./features/vendas/pdv.component').then(m => m.PdvComponent) },
      { path: 'clientes', loadComponent: () => import('./components/clientes/clientes-list/clientes-list.component').then(m => m.ClientesListComponent) },
      { path: 'estoque', loadComponent: () => import('./components/estoque/estoque-list/estoque-list.component').then(m => m.EstoqueListComponent) },
      { path: 'funcionarios', loadComponent: () => import('./components/funcionarios/funcionarios-list/funcionarios-list.component').then(m => m.FuncionariosListComponent) },
      {
        path: 'produto',
        loadComponent: () => import('./components/layout/produto/produto.component').then(m => m.ProdutoComponent),
        children: [
          { path: '', loadComponent: () => import('./components/layout/lista-produtos/lista-produtos.component').then(m => m.ListaProdutosComponent) },
        ],
      },
    ],
  },
  { path: '**', redirectTo: 'login' },
];




/*import { Routes } from '@angular/router';
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

];*/