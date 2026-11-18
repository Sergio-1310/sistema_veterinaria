import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catalogo, texto } from '../core/catalogo';
import { Modulo, Registro } from '../core/models';
@Component({
  selector: 'hv-tabla',
  imports: [RouterLink],
  template: ` <div class="scroll">
      <table>
        <thead>
          <tr>
            @for (c of config().columnas; track c[0]) {
              <th scope="col">{{ c[1] }}</th>
            }
            <th scope="col"><span class="sr">Detalle</span></th>
          </tr>
        </thead>
        <tbody>
          @for (fila of filas(); track fila.id) {
            <tr>
              @for (c of config().columnas; track c[0]; let primera = $first) {
                <td>
                  @if (primera) {
                    <a class="nombre" [routerLink]="['/', modulo(), fila.id]">{{ texto(fila, c[0]) }}</a>
                  } @else if (c[0] === 'activo' || c[0] === 'estado') {
                    <span class="badge" [attr.data-estado]="texto(fila, c[0])">{{ texto(fila, c[0]) }}</span>
                  } @else {
                    {{ texto(fila, c[0]) }}
                  }
                </td>
              }
              <td>
                <a
                  class="link"
                  [routerLink]="['/', modulo(), fila.id]"
                  [attr.aria-label]="'Ver detalle ' + fila.id"
                  >Ver detalle</a
                >
              </td>
            </tr>
          }
        </tbody>
      </table>
    </div>
    @if (!filas().length) {
      <p class="vacio">{{ vacio() }}</p>
    }`,
})
export class Tabla {
  modulo = input.required<Modulo>();
  filas = input.required<Registro[]>();
  vacio = input('No se encontraron registros.');
  texto = texto;
  config() {
    return catalogo[this.modulo()];
  }
}
