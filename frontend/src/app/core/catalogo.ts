import { Modulo, Registro } from './models';

export type Columna = readonly [string, string];
export interface Seccion {
  modulo: Modulo;
  titulo: string;
  uno: string;
  alta: string;
  descripcion: string;
  columnas: Columna[];
  extras?: Columna[];
}
export const catalogo: Record<Modulo, Seccion> = {
  propietarios: {
    modulo: 'propietarios',
    titulo: 'Propietarios',
    uno: 'Propietario',
    alta: 'Registrar propietario',
    descripcion: 'Contactos y mascotas asociadas.',
    columnas: [
      ['completo', 'Nombre completo'],
      ['correo', 'Correo electrónico'],
      ['telefono', 'Teléfono'],
    ],
  },
  mascotas: {
    modulo: 'mascotas',
    titulo: 'Mascotas',
    uno: 'Mascota',
    alta: 'Registrar mascota',
    descripcion: 'Pacientes registrados en la clínica.',
    columnas: [
      ['nombre', 'Nombre'],
      ['especie', 'Especie'],
      ['raza', 'Raza'],
      ['propietarioNombre', 'Propietario'],
    ],
    extras: [
      ['sexo', 'Sexo'],
      ['fechaNacimiento', 'Fecha de nacimiento'],
      ['edad', 'Edad registrada (años)'],
      ['nombrePropietario', 'Propietario en el registro anterior'],
    ],
  },
  citas: {
    modulo: 'citas',
    titulo: 'Citas',
    uno: 'Cita',
    alta: 'Programar cita',
    descripcion: 'Agenda de consultas veterinarias.',
    columnas: [
      ['mascotaNombre', 'Mascota'],
      ['veterinarioNombre', 'Veterinario'],
      ['fecha', 'Fecha'],
      ['hora', 'Hora'],
      ['estado', 'Estado'],
    ],
    extras: [['motivo', 'Motivo de consulta']],
  },
  veterinarios: {
    modulo: 'veterinarios',
    titulo: 'Veterinarios',
    uno: 'Veterinario',
    alta: 'Registrar veterinario',
    descripcion: 'Equipo médico y especialidades.',
    columnas: [
      ['completo', 'Veterinario'],
      ['especialidad', 'Especialidad'],
      ['correo', 'Correo electrónico'],
      ['activo', 'Estado'],
    ],
    extras: [['telefono', 'Teléfono']],
  },
  historial: {
    modulo: 'historial',
    titulo: 'Historial clínico',
    uno: 'Atención veterinaria',
    alta: '',
    descripcion: 'Atenciones finalizadas de los pacientes.',
    columnas: [
      ['cita.mascotaNombre', 'Mascota'],
      ['fecha', 'Fecha de atención'],
      ['cita.veterinarioNombre', 'Veterinario'],
      ['diagnostico', 'Diagnóstico'],
    ],
    extras: [
      ['cita.motivo', 'Motivo'],
      ['tratamiento', 'Tratamiento'],
      ['observaciones', 'Observaciones'],
    ],
  },
  usuarios: {
    modulo: 'usuarios',
    titulo: 'Usuarios',
    uno: 'Usuario',
    alta: 'Crear usuario',
    descripcion: 'Cuentas y permisos de acceso al sistema.',
    columnas: [
      ['nombre', 'Nombre'],
      ['email', 'Correo electrónico'],
      ['rol', 'Rol'],
      ['activo', 'Estado'],
    ],
  },
};
export function valor(obj: object, clave: string): unknown {
  if (clave === 'completo') return [valor(obj, 'nombres'), valor(obj, 'apellidos')].filter(Boolean).join(' ');
  return clave
    .split('.')
    .reduce<unknown>(
      (actual, k) => (actual && typeof actual === 'object' ? Reflect.get(actual, k) : undefined),
      obj,
    );
}
export function texto(obj: Registro, clave: string): string {
  const dato = valor(obj, clave);
  if (dato === null || dato === undefined || dato === '') return 'Sin registrar';
  if (typeof dato === 'boolean') return dato ? 'Activo' : 'Inactivo';
  if ((clave === 'fecha' || clave === 'fechaNacimiento') && /^\d{4}-\d{2}-\d{2}$/.test(String(dato)))
    return String(dato).split('-').reverse().join('/');
  if (clave === 'hora') return String(dato).slice(0, 5);
  if (clave === 'estado' || clave === 'rol')
    return String(dato).charAt(0) + String(dato).slice(1).toLowerCase();
  return String(dato);
}
