import { Routes } from '@angular/router';
import { proteger } from './core/session';
import { Modulo } from './core/models';

const modulos: Modulo[] = ['propietarios', 'mascotas', 'citas', 'veterinarios', 'historial', 'usuarios'];
const recepcion = ['ADMIN', 'RECEPCIONISTA'],
  clinica = ['ADMIN', 'VETERINARIO'];
export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'inicio' },
  { path: 'login', loadComponent: () => import('./features/login').then((m) => m.Login) },
  {
    path: 'inicio',
    canActivate: [proteger],
    loadComponent: () => import('./features/inicio').then((m) => m.Inicio),
  },
  ...modulos.flatMap((modulo): Routes => {
    const roles = modulo === 'usuarios' ? ['ADMIN'] : modulo === 'historial' ? clinica : undefined;
    const edicion = modulo === 'veterinarios' || modulo === 'usuarios' ? ['ADMIN'] : recepcion;
    const base = { canActivate: [proteger], runGuardsAndResolvers: 'always' as const };
    const formulario = () => import('./features/formulario').then((m) => m.Formulario);
    const lista = () => import('./features/listado').then((m) => m.Listado);
    const detalle = () => import('./features/detalle').then((m) => m.Detalle);
    return [
      { ...base, path: modulo, data: { modulo, roles }, loadComponent: lista },
      ...(modulo === 'historial'
        ? []
        : [
            { ...base, path: modulo + '/nuevo', data: { modulo, roles: edicion }, loadComponent: formulario },
            {
              ...base,
              path: modulo + '/:id/editar',
              data: { modulo, roles: edicion },
              loadComponent: formulario,
            },
          ]),
      { ...base, path: modulo + '/:id', data: { modulo, roles }, loadComponent: detalle },
    ];
  }),
  {
    path: 'citas/:id/atencion',
    data: { modulo: 'citas', accion: 'atencion', roles: clinica },
    canActivate: [proteger],
    loadComponent: () => import('./features/formulario').then((m) => m.Formulario),
  },
  {
    path: 'usuarios/:id/clave',
    data: { modulo: 'usuarios', accion: 'clave', roles: ['ADMIN'] },
    canActivate: [proteger],
    loadComponent: () => import('./features/formulario').then((m) => m.Formulario),
  },
  {
    path: 'prohibido',
    data: { titulo: 'Acceso denegado', texto: 'Tu rol no tiene acceso a esta sección.' },
    loadComponent: () => import('./features/aviso').then((m) => m.Aviso),
  },
  {
    path: '**',
    data: { titulo: 'Página no encontrada', texto: 'Revisa la dirección o vuelve al inicio.' },
    loadComponent: () => import('./features/aviso').then((m) => m.Aviso),
  },
];
