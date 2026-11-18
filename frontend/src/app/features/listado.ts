import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, map, of, switchMap, tap } from 'rxjs';
import { Modulo, Registro } from '../core/models';
import { catalogo } from '../core/catalogo';
import { Api } from '../core/api';
import { Sesion, mensaje } from '../core/session';
import { Tabla } from '../shared/tabla';
import { Icono } from '../shared/icono';

@Component({
  imports: [ReactiveFormsModule, RouterLink, Tabla, Icono],
  template: ` <div class="page">
    <header class="cabecera">
      <div>
        <h1>{{ cfg.titulo }}</h1>
        <p>{{ cfg.descripcion }}</p>
      </div>
      @if (puedeCrear()) {
        <a class="btn" [routerLink]="['/', modulo, 'nuevo']">＋ {{ cfg.alta }}</a>
      }
    </header>
    @if (aviso) {
      <p class="exito" role="status">{{ aviso }}</p>
    }
    <section class="card">
      <form class="filtros" [formGroup]="form" (ngSubmit)="filtrar()">
        <label class="busqueda"
          ><span class="sr">Buscar</span><hv-icono nombre="buscar" /><input
            formControlName="q"
            [placeholder]="
              modulo === 'historial' ? 'Buscar por mascota' : 'Buscar en ' + cfg.titulo.toLowerCase()
            "
        /></label>
        @if (modulo === 'citas') {
          <label>Fecha<input type="date" formControlName="fecha" /></label
          ><label
            >Estado<select formControlName="estado">
              <option value="">Todos</option>
              @for (e of estados; track e) {
                <option>{{ e }}</option>
              }
            </select></label
          >
        }
        @if (modulo === 'mascotas') {
          <label>Especie<input formControlName="especie" placeholder="Todas" /></label>
        }
        <button class="btn secundaria">Buscar</button
        ><button type="button" class="plano" (click)="limpiar()">Limpiar</button>
      </form>
      @if (cargando()) {
        <p class="vacio" role="status">Cargando registros…</p>
      } @else if (error()) {
        <p class="alert" role="alert">{{ error() }}</p>
      } @else {
        <hv-tabla [modulo]="modulo" [filas]="filas()" />
      }
    </section>
  </div>`,
})
export class Listado {
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private api = inject(Api);
  private sesion = inject(Sesion);
  modulo = this.ruta.snapshot.data['modulo'] as Modulo;
  cfg = catalogo[this.modulo];
  estados = ['PROGRAMADA', 'ATENDIDA', 'CANCELADA'];
  aviso = history.state?.aviso;
  form = inject(FormBuilder).nonNullable.group({ q: '', fecha: '', estado: '', especie: '' });
  filas = signal<Registro[]>([]);
  error = signal('');
  cargando = signal(true);
  constructor() {
    this.ruta.queryParamMap
      .pipe(
        tap((p) => {
          this.form.reset({
            q: p.get('q') || '',
            fecha: p.get('fecha') || '',
            estado: p.get('estado') || '',
            especie: p.get('especie') || '',
          });
          this.cargando.set(true);
          this.error.set('');
        }),
        switchMap(() => {
          const filtros: Record<string, string> = {};
          for (const [k, v] of Object.entries(this.form.getRawValue()))
            if (v) filtros[this.modulo === 'propietarios' && k === 'q' ? 'buscar' : k] = v.trim();
          return this.api.listar(this.modulo, filtros).pipe(
            map((filas) =>
              this.modulo === 'usuarios' && filtros['q']
                ? filas.filter((x) =>
                    Object.values(x).join(' ').toLowerCase().includes(filtros['q'].toLowerCase()),
                  )
                : filas,
            ),
            catchError((e) => {
              this.error.set(mensaje(e));
              return of([]);
            }),
          );
        }),
        takeUntilDestroyed(),
      )
      .subscribe((filas) => {
        this.filas.set(filas);
        this.cargando.set(false);
      });
  }
  puedeCrear() {
    return (
      this.modulo !== 'historial' &&
      (this.sesion.permite('ADMIN') ||
        (this.sesion.permite('RECEPCIONISTA') && ['citas', 'mascotas', 'propietarios'].includes(this.modulo)))
    );
  }
  filtrar() {
    const queryParams = Object.fromEntries(
      Object.entries(this.form.getRawValue()).filter(([, v]) => v.trim()),
    );
    return this.router.navigate([], { relativeTo: this.ruta, queryParams });
  }
  limpiar() {
    this.form.reset();
    void this.filtrar();
  }
}
