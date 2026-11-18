import { Component, input } from '@angular/core';

const trazos: Record<string, string> = {
  huella:
    'M7 14c1-1 2-5 5-5s4 4 5 5c3 4 0 7-3 5-1-.7-3-.7-4 0-4 2-6-1-3-5z M5 11c-2 0-3-4-1-4s3 4 1 4z M9 7c-2 0-3-5-1-5s3 5 1 5z M15 7c-2 0-1-5 1-5s1 5-1 5z M19 11c-2 0-1-4 1-4s1 4-1 4z',
  inicio: 'M3 3h7v7H3z M14 3h7v10h-7z M3 14h7v7H3z M14 17h7v4h-7z',
  propietarios:
    'M8 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8z M2 21v-2a6 6 0 0 1 12 0v2 M16 5a4 4 0 0 1 0 7 M17 15a5 5 0 0 1 5 5',
  citas: 'M3 6h18v15H3z M3 10h18 M7 3v5 M17 3v5 M7 14h3 M14 14h3',
  veterinarios:
    'M4 3v5a5 5 0 0 0 10 0V3 M3 3h3 M12 3h3 M9 13v3a5 5 0 0 0 10 0v-2 M19 14a2 2 0 1 0 0-4 2 2 0 0 0 0 4z',
  historial: 'M8 4H4v17h16V4h-4 M8 2h8v5H8z M8 11h8 M8 16h6',
  usuarios: 'M12 2l8 4v6c0 5-4 8-8 10-4-2-8-5-8-10V6z M8 12l3 3 5-6',
  reloj: 'M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20z M12 6v6l4 2',
  salir: 'M10 4H3v16h7 M9 12h12 M17 8l4 4-4 4',
  buscar: 'M10 17a7 7 0 1 0 0-14 7 7 0 0 0 0 14z M15 15l6 6',
  menu: 'M3 6h18 M3 12h18 M3 18h18',
};
@Component({
  selector: 'hv-icono',
  template: '<svg viewBox="0 0 24 24" aria-hidden="true"><path [attr.d]="trazo()" /></svg>',
  styles: [
    ':host{display:inline-flex}svg{width:1.22em;height:1.22em;fill:none;stroke:currentColor;stroke-width:1.7;stroke-linecap:round;stroke-linejoin:round}',
  ],
})
export class Icono {
  nombre = input.required<string>();
  trazo() {
    return trazos[this.nombre() === 'mascotas' ? 'huella' : this.nombre()];
  }
}
