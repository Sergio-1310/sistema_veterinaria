import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Sesion, mensaje } from '../core/session';
import { Icono } from '../shared/icono';

@Component({
  imports: [ReactiveFormsModule, Icono],
  template: ` <section class="login">
    <div class="identidad">
      <div class="sello"><hv-icono nombre="huella" /></div>
      <strong>HuellitasVet</strong>
      <p>Sistema de gestión veterinaria</p>
    </div>
    <div class="login-panel">
      <h1>Iniciar sesión</h1>
      <p>Ingresa tus credenciales para continuar.</p>
      <form [formGroup]="form" (ngSubmit)="entrar()">
        <label
          >Correo electrónico<input
            type="email"
            formControlName="email"
            autocomplete="username"
            placeholder="nombre@clinica.pe"
          />
          @if (form.controls.email.touched && form.controls.email.invalid) {
            <small class="error">Ingresa un correo válido.</small>
          }
        </label>
        <label
          >Contraseña<input type="password" formControlName="password" autocomplete="current-password" />
          @if (form.controls.password.touched && form.controls.password.invalid) {
            <small class="error">Ingresa tu contraseña.</small>
          }
        </label>
        @if (error()) {
          <p class="alert" role="alert">{{ error() }}</p>
        }
        <button class="btn" [disabled]="form.invalid || cargando()">
          {{ cargando() ? 'Ingresando…' : 'Ingresar' }}
        </button>
      </form>
    </div>
  </section>`,
})
export class Login {
  private sesion = inject(Sesion);
  private router = inject(Router);
  form = inject(FormBuilder).nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });
  cargando = signal(false);
  error = signal(
    inject(ActivatedRoute).snapshot.queryParamMap.get('fallo') === 'conexion'
      ? 'No se pudo conectar con la API.'
      : '',
  );
  async entrar() {
    if (this.form.invalid || this.cargando()) return;
    this.cargando.set(true);
    this.error.set('');
    try {
      const { email, password } = this.form.getRawValue();
      await this.sesion.entrar(email, password);
      await this.router.navigateByUrl('/inicio');
    } catch (e) {
      this.error.set(mensaje(e));
    } finally {
      this.cargando.set(false);
    }
  }
}
