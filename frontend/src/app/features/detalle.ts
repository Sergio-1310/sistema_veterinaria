import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { Api } from '../core/api';
import { catalogo, texto, valor } from '../core/catalogo';
import { Modulo, Registro } from '../core/models';
import { Sesion, mensaje } from '../core/session';
import { Tabla } from '../shared/tabla';

interface Relacion {
  modulo: Modulo;
  filas: Registro[];
}
@Component({
  imports: [RouterLink, Tabla],
  template: ` <div class="page">
    <a class="volver" [routerLink]="['/', modulo]">← {{ cfg.titulo }}</a>
    @if (error()) {
      <p class="alert" role="alert">{{ error() }}</p>
    }
    @if (aviso()) {
      <p class="exito" role="status">{{ aviso() }}</p>
    }
    @if (cargando()) {
      <p role="status">Cargando detalle…</p>
    } @else if (fila(); as item) {
      <header class="cabecera">
        <div>
          <small>{{ cfg.uno }} · #{{ item.id }}</small>
          <h1>{{ titulo(item) }}</h1>
        </div>
        <div class="acciones">
          @if (editable()) {
            <a class="btn secundaria" [routerLink]="['/', modulo, id, 'editar']">Editar</a>
          }
          @if (modulo === 'propietarios' && recepcion()) {
            <a class="btn" routerLink="/mascotas/nuevo" [queryParams]="{ propietarioId: id }"
              >Registrar mascota</a
            >
          }
          @if (modulo === 'mascotas' && recepcion()) {
            <a class="btn" routerLink="/citas/nuevo" [queryParams]="{ mascotaId: id }">Programar cita</a>
          }
          @if (puedeAtender()) {
            <a class="btn" [routerLink]="['/citas', id, 'atencion']">Registrar atención</a>
          }
        </div>
      </header>
      <section class="card ficha">
        <h2>Datos de {{ cfg.uno.toLowerCase() }}</h2>
        <dl>
          @for (c of columnas; track c[0]) {
            <div>
              <dt>{{ c[1] }}</dt>
              <dd>{{ texto(item, c[0]) }}</dd>
            </div>
          }
        </dl>
        <div class="acciones">
          @if (modulo === 'mascotas' && valor(item, 'propietarioId')) {
            <a [routerLink]="['/propietarios', valor(item, 'propietarioId')]">Ver propietario</a>
          }
          @if (modulo === 'citas' && valor(item, 'mascotaId')) {
            <a [routerLink]="['/mascotas', valor(item, 'mascotaId')]">Ver mascota</a>
          }
          @if (modulo === 'historial') {
            <a [routerLink]="['/citas', valor(item, 'cita.id')]">Ver cita</a>
          }
          @if (modulo === 'usuarios' && sesion.permite('ADMIN')) {
            <a class="btn secundaria" [routerLink]="['/usuarios', id, 'clave']">Cambiar contraseña</a>
          }
          @if (accion()) {
            <button class="btn secundaria" (click)="modal.showModal()">{{ accion() }}</button>
          }
        </div>
      </section>
      @for (r of relacionados(); track r.modulo) {
        <section class="card">
          <div class="section-head">
            <h2>{{ nombreSeccion(r.modulo) }}</h2>
          </div>
          <hv-tabla [modulo]="r.modulo" [filas]="r.filas" />
        </section>
      }
      <dialog #modal>
        <h2>{{ accion() }}</h2>
        <p>¿Confirmas esta operación para el registro #{{ id }}?</p>
        <div class="acciones">
          <button class="btn secundaria" (click)="modal.close()">Volver</button
          ><button class="btn" [disabled]="ocupado()" (click)="confirmar(modal)">Confirmar</button>
        </div>
      </dialog>
    }
  </div>`,
})
export class Detalle {
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private api = inject(Api);
  sesion = inject(Sesion);
  modulo = this.ruta.snapshot.data['modulo'] as Modulo;
  cfg = catalogo[this.modulo];
  id = Number(this.ruta.snapshot.paramMap.get('id'));
  columnas = [...this.cfg.columnas, ...(this.cfg.extras ?? [])];
  valor = valor;
  texto = texto;
  fila = signal<Registro | null>(null);
  relacionados = signal<Relacion[]>([]);
  cargando = signal(true);
  ocupado = signal(false);
  error = signal('');
  aviso = signal<string>(history.state?.aviso || '');
  responsable = signal(false);
  constructor() {
    void this.cargar();
  }
  nombreSeccion(modulo: Modulo) {
    return catalogo[modulo].titulo;
  }
  titulo(item: Registro) {
    return this.modulo === 'citas'
      ? 'Consulta de ' + texto(item, 'mascotaNombre')
      : texto(item, this.cfg.columnas[0][0]);
  }
  recepcion() {
    return this.sesion.permite('ADMIN', 'RECEPCIONISTA');
  }
  editable() {
    return (
      this.modulo !== 'historial' &&
      (this.modulo !== 'citas' || this.estado() === 'PROGRAMADA') &&
      (['usuarios', 'veterinarios'].includes(this.modulo) ? this.sesion.permite('ADMIN') : this.recepcion())
    );
  }
  estado() {
    const f = this.fila();
    return f && valor(f, 'estado');
  }
  puedeAtender() {
    return (
      this.modulo === 'citas' &&
      this.estado() === 'PROGRAMADA' &&
      (this.sesion.permite('ADMIN') || this.responsable())
    );
  }
  accion() {
    const f = this.fila();
    if (!f) return '';
    if (this.modulo === 'citas' && this.editable()) return 'Cancelar cita';
    if (['mascotas', 'propietarios'].includes(this.modulo) && this.recepcion())
      return 'Eliminar ' + this.cfg.uno.toLowerCase();
    if (['veterinarios', 'usuarios'].includes(this.modulo) && this.sesion.permite('ADMIN'))
      return valor(f, 'activo') ? 'Desactivar' : 'Activar';
    return '';
  }
  async cargar() {
    this.cargando.set(true);
    this.error.set('');
    try {
      const item = await firstValueFrom(this.api.detalle(this.modulo, this.id));
      this.fila.set(item);
      const grupos: Promise<Relacion>[] = [];
      const relacion = (modulo: Modulo, params: Record<string, string>) =>
        grupos.push(firstValueFrom(this.api.listar(modulo, params)).then((filas) => ({ modulo, filas })));
      if (this.modulo === 'propietarios') relacion('mascotas', { propietarioId: String(this.id) });
      if (this.modulo === 'mascotas') {
        relacion('citas', { mascotaId: String(this.id) });
        if (this.sesion.permite('ADMIN', 'VETERINARIO'))
          relacion('historial', { mascotaId: String(this.id) });
      }
      if (this.modulo === 'veterinarios') relacion('citas', { veterinarioId: String(this.id) });
      if (this.modulo === 'citas' && this.sesion.permite('VETERINARIO') && valor(item, 'veterinarioId')) {
        const vet = await firstValueFrom(
          this.api.detalle('veterinarios', Number(valor(item, 'veterinarioId'))),
        );
        this.responsable.set(vet.correo.toLowerCase() === this.sesion.usuario()?.email.toLowerCase());
      }
      this.relacionados.set(await Promise.all(grupos));
    } catch (e) {
      this.error.set(mensaje(e));
    } finally {
      this.cargando.set(false);
    }
  }
  async confirmar(dialogo: HTMLDialogElement) {
    const fila = this.fila();
    if (!fila || this.ocupado()) return;
    this.ocupado.set(true);
    this.error.set('');
    try {
      if (this.modulo === 'usuarios' || this.modulo === 'veterinarios') {
        await firstValueFrom(this.api.activar(this.modulo, this.id, !valor(fila, 'activo')));
        dialogo.close();
        await this.cargar();
        this.aviso.set('Estado actualizado.');
      } else {
        await firstValueFrom(this.api.eliminar(this.modulo, this.id));
        dialogo.close();
        await this.router.navigate(['/', this.modulo], {
          state: { aviso: this.modulo === 'citas' ? 'Cita cancelada.' : 'Registro eliminado.' },
        });
      }
    } catch (e) {
      dialogo.close();
      this.error.set(mensaje(e));
    } finally {
      this.ocupado.set(false);
    }
  }
}
