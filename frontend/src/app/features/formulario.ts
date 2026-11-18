import { Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormRecord,
  ReactiveFormsModule,
  Validators,
  ValidatorFn,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { Api } from '../core/api';
import { Modulo, Opcion, fechaLocal } from '../core/models';
import { catalogo, valor } from '../core/catalogo';
import { camposDe, Campo } from '../core/campos';
import { mensaje } from '../core/session';

@Component({
  imports: [ReactiveFormsModule, RouterLink],
  template: ` <div class="page">
    <a class="volver" [routerLink]="volver()">← Volver a {{ cfg.titulo.toLowerCase() }}</a>
    <header class="cabecera">
      <div>
        <h1>{{ titulo }}</h1>
        @if (contexto()) {
          <p>{{ contexto() }}</p>
        }
      </div>
    </header>
    @if (error()) {
      <p class="alert" role="alert">{{ error() }}</p>
    }
    @if (cargando()) {
      <p role="status">Cargando formulario…</p>
    } @else if (listo()) {
      <form class="card formulario" [formGroup]="form" (ngSubmit)="guardar()">
        <div class="campos">
          @for (c of campos; track c.clave) {
            <label [class.ancho]="c.tipo === 'textarea'"
              >{{ c.etiqueta }}
              @if (c.opcional) {
                <small>(opcional)</small>
              }
              @switch (c.tipo) {
                @case ('select') {
                  <select
                    [formControlName]="c.clave"
                    [attr.aria-label]="c.etiqueta"
                    [attr.aria-describedby]="
                      form.controls[c.clave].touched && form.controls[c.clave].invalid
                        ? 'error-' + c.clave
                        : null
                    "
                  >
                    <option value="">Seleccionar</option>
                    @for (o of opciones[c.clave]; track o.valor) {
                      <option [value]="o.valor">{{ o.nombre }}</option>
                    }
                  </select>
                }
                @case ('textarea') {
                  <textarea
                    rows="3"
                    [formControlName]="c.clave"
                    [attr.aria-label]="c.etiqueta"
                    [attr.aria-describedby]="
                      form.controls[c.clave].touched && form.controls[c.clave].invalid
                        ? 'error-' + c.clave
                        : null
                    "
                    [attr.maxlength]="c.max"
                  ></textarea>
                }
                @default {
                  <input
                    [type]="c.tipo || 'text'"
                    [formControlName]="c.clave"
                    [attr.aria-label]="c.etiqueta"
                    [attr.aria-describedby]="
                      form.controls[c.clave].touched && form.controls[c.clave].invalid
                        ? 'error-' + c.clave
                        : null
                    "
                    [attr.maxlength]="c.max || null"
                    [attr.min]="c.clave === 'fecha' ? hoy : null"
                    [attr.max]="c.clave === 'fechaNacimiento' ? hoy : null"
                    [attr.autocomplete]="c.tipo === 'password' ? 'new-password' : null"
                  />
                }
              }
              @if (form.controls[c.clave].touched && form.controls[c.clave].invalid) {
                <small class="error" [id]="'error-' + c.clave">{{ errorCampo(c) }}</small>
              }
              @if (
                c.clave === 'confirmacion' && form.hasError('coincidencia') && form.controls[c.clave].touched
              ) {
                <small class="error">Las contraseñas deben coincidir.</small>
              }
              @if (c.clave === 'hora' && form.hasError('pasado')) {
                <small class="error">Elige un horario futuro.</small>
              }
              @if (c.tipo === 'select' && !c.opcional && !opciones[c.clave]?.length) {
                <small class="error">No hay opciones disponibles para este campo.</small>
              }
            </label>
          }
        </div>
        <footer class="acciones">
          <a class="btn secundaria" [routerLink]="volver()">Volver</a
          ><button class="btn" [disabled]="form.invalid || ocupado()">
            {{ ocupado() ? 'Guardando…' : accion === 'atencion' ? 'Finalizar atención' : 'Guardar' }}
          </button>
        </footer>
      </form>
    }
  </div>`,
})
export class Formulario {
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private api = inject(Api);
  modulo = this.ruta.snapshot.data['modulo'] as Modulo;
  cfg = catalogo[this.modulo];
  accion = String(this.ruta.snapshot.data['accion'] || '');
  id = this.ruta.snapshot.paramMap.has('id') ? Number(this.ruta.snapshot.paramMap.get('id')) : null;
  titulo =
    this.accion === 'atencion'
      ? 'Registrar atención'
      : this.accion === 'clave'
        ? 'Cambiar contraseña'
        : this.id
          ? 'Editar ' + this.cfg.uno.toLowerCase()
          : this.cfg.alta;
  campos = camposDe(this.modulo, this.accion, this.id === null);
  form = new FormRecord<FormControl<string>>({});
  opciones: Record<string, Opcion[]> = {};
  hoy = fechaLocal();
  error = signal('');
  cargando = signal(true);
  ocupado = signal(false);
  listo = signal(false);
  contexto = signal('');
  constructor() {
    for (const c of this.campos) {
      const reglas: ValidatorFn[] = [];
      if (!c.opcional) reglas.push(Validators.required, Validators.pattern(/[\s\S]*\S[\s\S]*/));
      if (c.max) reglas.push(Validators.maxLength(c.max));
      if (c.tipo === 'email') reglas.push(Validators.email);
      if (c.tipo === 'tel') reglas.push(Validators.pattern(/^(?=(?:[^0-9]*[0-9]){7})[+\d() -]{7,20}$/));
      if (c.tipo === 'password')
        reglas.push(Validators.minLength(10), (control) =>
          new TextEncoder().encode(control.value).length > 72 ? { bytes: true } : null,
        );
      if (c.clave === 'fechaNacimiento')
        reglas.push((control) => (control.value > this.hoy ? { futura: true } : null));
      this.form.addControl(c.clave, new FormControl('', { nonNullable: true, validators: reglas }));
      this.opciones[c.clave] = (c.opciones || []).map((v) => ({ valor: v, nombre: v }));
    }
    if (this.accion === 'clave')
      this.form.addValidators((g) =>
        g.value.password === g.value.confirmacion ? null : { coincidencia: true },
      );
    if (this.modulo === 'citas' && !this.accion)
      this.form.addValidators((g: AbstractControl) =>
        g.value.fecha && g.value.hora && new Date(g.value.fecha + 'T' + g.value.hora) < new Date()
          ? { pasado: true }
          : null,
      );
    void this.cargar();
  }
  volver() {
    return this.id ? ['/', this.modulo, this.id] : ['/', this.modulo];
  }
  async cargar() {
    try {
      if (this.form.contains('propietarioId'))
        this.opciones['propietarioId'] = (await firstValueFrom(this.api.listar('propietarios'))).map((p) => ({
          valor: String(p.id),
          nombre: p.nombres + ' ' + p.apellidos + ' · ' + p.correo,
        }));
      if (this.form.contains('veterinarioId')) {
        const [mascotas, vets] = await Promise.all([
          firstValueFrom(this.api.listar('mascotas')),
          firstValueFrom(this.api.listar('veterinarios', { activo: 'true' })),
        ]);
        this.opciones['mascotaId'] = mascotas.map((p) => ({
          valor: String(p.id),
          nombre: p.nombre + ' · ' + (p.propietarioNombre || '#' + p.id),
        }));
        this.opciones['veterinarioId'] = vets.map((v) => ({
          valor: String(v.id),
          nombre: v.nombres + ' ' + v.apellidos,
        }));
      }
      if (this.id !== null) {
        const item = await firstValueFrom(this.api.detalle(this.modulo, this.id));
        if (this.modulo === 'citas' && valor(item, 'estado') !== 'PROGRAMADA')
          throw new Error('Esta cita ya no está programada.');
        if (this.accion === 'atencion')
          this.contexto.set(
            [valor(item, 'mascotaNombre'), valor(item, 'veterinarioNombre'), valor(item, 'motivo')]
              .filter(Boolean)
              .join(' · '),
          );
        for (const c of this.campos) this.form.controls[c.clave].setValue(String(valor(item, c.clave) ?? ''));
      } else {
        for (const key of ['propietarioId', 'mascotaId'])
          if (this.form.contains(key))
            this.form.controls[key].setValue(this.ruta.snapshot.queryParamMap.get(key) || '');
      }
      this.listo.set(true);
    } catch (e) {
      this.error.set(
        e instanceof HttpErrorResponse ? mensaje(e) : e instanceof Error ? e.message : mensaje(e),
      );
    } finally {
      this.cargando.set(false);
    }
  }
  errorCampo(c: Campo) {
    const e = this.form.controls[c.clave].errors || {};
    if (e['servidor']) return e['servidor'];
    if (e['required']) return 'Este campo es obligatorio.';
    if (e['email']) return 'Ingresa un correo electrónico válido.';
    if (e['minlength'] || e['bytes']) return 'Usa al menos 10 caracteres y un máximo de 72 bytes.';
    if (e['maxlength']) return 'El texto supera el máximo permitido.';
    if (e['futura']) return 'La fecha no puede ser futura.';
    return c.tipo === 'tel' ? 'Ingresa al menos 7 dígitos y un máximo de 20 caracteres.' : 'Revisa este valor.';
  }
  async guardar() {
    this.form.updateValueAndValidity();
    if (this.form.invalid || this.ocupado() || !this.listo()) return;
    this.ocupado.set(true);
    this.error.set('');
    const datos: Record<string, unknown> = {};
    for (const [k, v] of Object.entries(this.form.getRawValue()))
      datos[k] = k.endsWith('Id') ? Number(v) : v || null;
    try {
      let destino = this.volver();
      if (this.accion === 'atencion') {
        const f = await firstValueFrom(this.api.atender(this.id!, datos));
        destino = ['/historial', f.id];
      } else if (this.accion === 'clave') {
        await firstValueFrom(this.api.clave(this.id!, String(datos['password'])));
      } else {
        const item = await firstValueFrom(this.api.guardar(this.modulo, this.id, datos));
        destino = ['/', this.modulo, item.id];
      }
      await this.router.navigate(destino, { state: { aviso: 'Cambios guardados.' } });
    } catch (e) {
      this.error.set(mensaje(e));
      if (e instanceof HttpErrorResponse)
        for (const [k, v] of Object.entries(e.error?.campos || {})) {
          const ctrl = this.form.controls[k];
          if (ctrl) {
            ctrl.setErrors({ servidor: String(v) });
            ctrl.markAsTouched();
          }
        }
    } finally {
      this.ocupado.set(false);
    }
  }
}
