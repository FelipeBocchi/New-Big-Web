import { Routes } from '@angular/router';
import { Login } from './layout/login/login.component';
import { FuncionariosComponent } from './funcionarios/funcionarios.component';

export const routes: Routes = [
  {
    path: 'login',
    component: Login
  },

  {
    path: 'funcionarios',
    component: FuncionariosComponent
  },

  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },

  {
    path: '**',
    redirectTo: 'login'
  }
];