import { inject, Injectable, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { Router, CanActivateFn } from '@angular/router';
import { catchError, firstValueFrom, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { Rol, Usuario } from './models';

const tokenKey = 'huellitas.sesion';
@Injectable({ providedIn: 'root' })
export class Sesion {
  private http = inject(HttpClient);
  private router = inject(Router);
  usuario = signal<Usuario | null>(null);
  token() {
    return sessionStorage.getItem(tokenKey);
  }
  async entrar(email: string, password: string) {
    const datos = await firstValueFrom(
      this.http.post<{ token: string; usuario: Usuario }>(environment.api + '/auth/login', {
        email,
        password,
      }),
    );
    sessionStorage.setItem(tokenKey, datos.token);
    this.usuario.set(datos.usuario);
  }
  async perfil() {
    if (!this.token()) return null;
    if (!this.usuario())
      this.usuario.set(await firstValueFrom(this.http.get<Usuario>(environment.api + '/auth/me')));
    return this.usuario();
  }
  salir() {
    sessionStorage.removeItem(tokenKey);
    this.usuario.set(null);
    void this.router.navigateByUrl('/login');
  }
  permite(...roles: Rol[]) {
    const actual = this.usuario();
    return !!actual && roles.includes(actual.rol);
  }
}

export const proteger: CanActivateFn = async (ruta) => {
  const sesion = inject(Sesion),
    router = inject(Router);
  try {
    const usuario = await sesion.perfil();
    if (!usuario) return router.parseUrl('/login');
    const roles = ruta.data['roles'] as Rol[] | undefined;
    return !roles || roles.includes(usuario.rol) || router.parseUrl('/prohibido');
  } catch (e) {
    return router.createUrlTree(['/login'], {
      queryParams: { fallo: e instanceof HttpErrorResponse && e.status === 0 ? 'conexion' : 'sesion' },
    });
  }
};

export const autenticacion: HttpInterceptorFn = (req, next) => {
  const sesion = inject(Sesion);
  const local = req.url.startsWith(environment.api + '/') && req.url !== environment.api + '/auth/login';
  const token = local ? sesion.token() : null;
  return next(token ? req.clone({ setHeaders: { Authorization: 'Bearer ' + token } }) : req).pipe(
    catchError((e) => {
      if (local && e.status === 401 && token === sesion.token()) sesion.salir();
      return throwError(() => e);
    }),
  );
};

export function mensaje(e: unknown): string {
  if (e instanceof HttpErrorResponse)
    return (
      e.error?.mensaje ||
      (e.status === 0 ? 'No se pudo conectar con la API.' : 'No se pudo completar la operación.')
    );
  return 'No se pudo completar la operación.';
}
