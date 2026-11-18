import { Modulo } from './models';

export interface Campo {
  clave: string;
  etiqueta: string;
  tipo?: 'email' | 'tel' | 'date' | 'time' | 'password' | 'select' | 'textarea';
  opcional?: boolean;
  max?: number;
  opciones?: string[];
}
const persona: Campo[] = [
  { clave: 'nombres', etiqueta: 'Nombres', max: 100 },
  { clave: 'apellidos', etiqueta: 'Apellidos', max: 100 },
];
const telefono: Campo = { clave: 'telefono', etiqueta: 'Teléfono', tipo: 'tel', max: 20 };
const correo: Campo = { clave: 'correo', etiqueta: 'Correo electrónico', tipo: 'email', max: 100 };
const password: Campo = { clave: 'password', etiqueta: 'Contraseña nueva', tipo: 'password', max: 72 };
export function camposDe(modulo: Modulo, accion: string, crear: boolean): Campo[] {
  if (accion === 'atencion')
    return [
      { clave: 'diagnostico', etiqueta: 'Diagnóstico', tipo: 'textarea', max: 2000 },
      { clave: 'tratamiento', etiqueta: 'Tratamiento', tipo: 'textarea', max: 2000 },
      { clave: 'observaciones', etiqueta: 'Observaciones', tipo: 'textarea', opcional: true, max: 2000 },
    ];
  if (accion === 'clave')
    return [password, { ...password, clave: 'confirmacion', etiqueta: 'Confirmar contraseña' }];
  switch (modulo) {
    case 'propietarios':
      return [...persona, correo, telefono];
    case 'veterinarios':
      return [
        ...persona,
        { clave: 'especialidad', etiqueta: 'Especialidad', opcional: true, max: 100 },
        { ...correo, max: 120 },
        { ...telefono, opcional: true },
      ];
    case 'mascotas':
      return [
        { clave: 'nombre', etiqueta: 'Nombre', max: 100 },
        { clave: 'especie', etiqueta: 'Especie', max: 80 },
        { clave: 'raza', etiqueta: 'Raza', opcional: true, max: 100 },
        { clave: 'fechaNacimiento', etiqueta: 'Fecha de nacimiento', tipo: 'date', opcional: true },
        {
          clave: 'sexo',
          etiqueta: 'Sexo',
          tipo: 'select',
          opciones: ['Macho', 'Hembra', 'Desconocido'],
          opcional: true,
        },
        { clave: 'propietarioId', etiqueta: 'Propietario', tipo: 'select' },
      ];
    case 'citas':
      return [
        { clave: 'mascotaId', etiqueta: 'Mascota', tipo: 'select' },
        { clave: 'veterinarioId', etiqueta: 'Veterinario responsable', tipo: 'select' },
        { clave: 'fecha', etiqueta: 'Fecha', tipo: 'date' },
        { clave: 'hora', etiqueta: 'Hora', tipo: 'time' },
        { clave: 'motivo', etiqueta: 'Motivo de consulta', tipo: 'textarea', max: 255 },
      ];
    case 'usuarios':
      return [
        { clave: 'nombre', etiqueta: 'Nombre completo', max: 100 },
        { ...correo, clave: 'email' },
        {
          clave: 'rol',
          etiqueta: 'Rol',
          tipo: 'select',
          opciones: ['ADMIN', 'RECEPCIONISTA', 'VETERINARIO'],
        },
        ...(crear ? [password] : []),
      ];
    default:
      return [];
  }
}
