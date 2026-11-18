import { Component, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { RouterLink } from '@angular/router';
import { Api } from '../core/api';
import { Dashboard } from '../core/models';
import { Sesion, mensaje } from '../core/session';
import { Tabla } from '../shared/tabla';
import { Icono } from '../shared/icono';
@Component({
  imports: [RouterLink, Tabla, Icono],
  template: ` <div class="page">
    <header class="cabecera">
      <div>
        <h1>Hola, {{ sesion.usuario()?.nombre }}</h1>
        <p>Actividad de la clínica · {{ fecha }}</p>
      </div>
    </header>
    @if (error()) {
      <p class="alert" role="alert">{{ error() }} <button (click)="cargar()">Reintentar</button></p>
    } @else if (datos(); as d) {
      <section class="metricas">
        @for (m of metricas; track m.clave) {
          <article class="card metrica">
            <span class="ico" [class]="'ico ' + m.clave"><hv-icono [nombre]="m.icono" /></span>
            <div>
              <small>{{ m.nombre }}</small
              ><strong>{{ d[m.clave] }}</strong>
            </div>
          </article>
        }
      </section>
      <section class="card">
        <div class="section-head">
          <h2>Próximas citas</h2>
          <a routerLink="/citas">Ver agenda →</a>
        </div>
        <hv-tabla modulo="citas" [filas]="d.proximas" vacio="No hay citas próximas programadas." />
      </section>
      @if (sesion.permite('ADMIN', 'RECEPCIONISTA')) {
        <section class="card accesos">
          <h2>Accesos rápidos</h2>
          <div class="acciones">
            <a class="btn" routerLink="/citas/nuevo">＋ Programar cita</a
            ><a class="btn secundaria" routerLink="/mascotas/nuevo">＋ Registrar mascota</a>
          </div>
        </section>
      }
    } @else {
      <p role="status">Cargando resumen…</p>
    }
  </div>`,
})
export class Inicio {
  sesion = inject(Sesion);
  private api = inject(Api);
  fecha = new Intl.DateTimeFormat('es-PE', { dateStyle: 'long' }).format(new Date());
  metricas = [
    { clave: 'mascotas', nombre: 'Mascotas registradas', icono: 'mascotas' },
    { clave: 'hoy', nombre: 'Citas para hoy', icono: 'citas' },
    { clave: 'pendientes', nombre: 'Citas pendientes', icono: 'reloj' },
  ] as const;
  datos = signal<Dashboard | null>(null);
  error = signal('');
  constructor() {
    void this.cargar();
  }
  async cargar() {
    this.error.set('');
    try {
      this.datos.set(await firstValueFrom(this.api.panel()));
    } catch (e) {
      this.error.set(mensaje(e));
    }
  }
}
