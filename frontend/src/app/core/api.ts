import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Dashboard, Ficha, Modulo, Recursos } from './models';

@Injectable({ providedIn: 'root' })
export class Api {
  private http = inject(HttpClient);
  private base = environment.api;
  listar<K extends Modulo>(modulo: K, params: Record<string, string> = {}) {
    return this.http.get<Recursos[K][]>(this.base + '/' + modulo, { params });
  }
  detalle<K extends Modulo>(modulo: K, id: number) {
    return this.http.get<Recursos[K]>(this.base + '/' + modulo + '/' + id);
  }
  guardar<K extends Modulo>(modulo: K, id: number | null, datos: Record<string, unknown>) {
    const url = this.base + '/' + modulo;
    return id === null
      ? this.http.post<Recursos[K]>(url, datos)
      : this.http.put<Recursos[K]>(url + '/' + id, datos);
  }
  eliminar(modulo: Modulo, id: number) {
    return this.http.delete<void>(this.base + '/' + modulo + '/' + id);
  }
  activar(modulo: 'usuarios' | 'veterinarios', id: number, activo: boolean) {
    return this.http.patch(this.base + '/' + modulo + '/' + id + '/activo', { activo });
  }
  clave(id: number, password: string) {
    return this.http.put<void>(this.base + '/usuarios/' + id + '/password', { password });
  }
  atender(id: number, datos: Record<string, unknown>) {
    return this.http.post<Ficha>(this.base + '/citas/' + id + '/atencion', datos);
  }
  panel() {
    return this.http.get<Dashboard>(this.base + '/dashboard');
  }
}
