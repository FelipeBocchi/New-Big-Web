import { Component, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-menu',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.scss',
})
export class MenuComponent {
  protected readonly menuOpen = signal(false);

  protected readonly links = [
    { label: 'Dashboard', route: null },
    { label: 'Caixa', route: '/newBig/cash' },
    { label: 'Lotes', route: '/newBig/estoque' },
    { label: 'Produto', route: '/newBig/produto' },
    { label: 'Funcionário', route: '/newBig/funcionarios' },
    { label: 'Cliente', route: '/newBig/clientes' },
  ];

  protected closeMenu(toggle: HTMLButtonElement): void {
    if (!this.menuOpen()) return;

    this.menuOpen.set(false);
    toggle.focus();
  }
}
