import { Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
@Component({
  imports: [RouterLink],
  template:
    '<section class="card aviso"><h1>{{datos["titulo"]}}</h1><p>{{datos["texto"]}}</p><a class="btn" routerLink="/inicio">Volver al inicio</a></section>',
})
export class Aviso {
  datos = inject(ActivatedRoute).snapshot.data;
}
