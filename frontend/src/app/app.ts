import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Sesion } from './core/session';
import { Icono } from './shared/icono';
import { catalogo } from './core/catalogo';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, Icono],
  templateUrl: './app.html',
})
export class App {
  sesion = inject(Sesion);
  menu = signal(false);
  catalogo = catalogo;
  get enlaces() {
    return Object.values(this.catalogo)
      .filter((x) => x.modulo !== 'usuarios' || this.sesion.permite('ADMIN'))
      .filter((x) => x.modulo !== 'historial' || this.sesion.permite('ADMIN', 'VETERINARIO'));
  }
}
