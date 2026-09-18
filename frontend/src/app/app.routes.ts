import { Routes } from '@angular/router';
import { LoginComponent } from './components/layout/login/login.component';
import { PrincipalComponent } from './components/layout/principal/principal.component';
import { CashMasterComponent } from './components/caixa/cash-master/cash-master.component';

export const routes: Routes = [

    { path: "", redirectTo: "login", pathMatch: "full"},
    { path: "login", component: LoginComponent},
    {
        path: "newBig", component: PrincipalComponent, children: [
            { path: "cash", component: CashMasterComponent}
            //aqui vai entrar os componentes de telas que tenham o menu lateral e topos os componentes em principal
        ]
    }

];