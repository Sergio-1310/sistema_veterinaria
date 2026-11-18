export type Rol = 'ADMIN' | 'RECEPCIONISTA' | 'VETERINARIO';
export interface Usuario {
  id: number;
  nombre: string;
  email: string;
  rol: Rol;
  activo: boolean;
}
export interface Propietario {
  id: number;
  nombres: string;
  apellidos: string;
  telefono: string;
  correo: string;
}
export interface Veterinario extends Propietario {
  especialidad: string | null;
  activo: boolean;
}
export interface Mascota {
  id: number;
  nombre: string;
  especie: string;
  raza: string | null;
  fechaNacimiento: string | null;
  edad: number | null;
  sexo: string | null;
  propietarioId: number | null;
  propietarioNombre: string | null;
  nombrePropietario: string | null;
}
export interface Cita {
  id: number;
  mascotaId: number | null;
  mascotaNombre: string | null;
  veterinarioId: number | null;
  veterinarioNombre: string | null;
  fecha: string | null;
  hora: string | null;
  motivo: string;
  estado: string;
}
export interface Ficha {
  id: number;
  fecha: string;
  cita: Cita;
  diagnostico: string;
  tratamiento: string;
  observaciones: string | null;
}
export interface Dashboard {
  mascotas: number;
  hoy: number;
  pendientes: number;
  proximas: Cita[];
}
export interface Recursos {
  propietarios: Propietario;
  mascotas: Mascota;
  citas: Cita;
  veterinarios: Veterinario;
  historial: Ficha;
  usuarios: Usuario;
}
export type Modulo = keyof Recursos;
export type Registro = Recursos[Modulo];
export interface Opcion {
  valor: string;
  nombre: string;
}
export function fechaLocal(fecha = new Date()): string {
  return [
    fecha.getFullYear(),
    String(fecha.getMonth() + 1).padStart(2, '0'),
    String(fecha.getDate()).padStart(2, '0'),
  ].join('-');
}
