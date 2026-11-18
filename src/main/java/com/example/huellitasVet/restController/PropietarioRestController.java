package com.example.huellitasVet.restController;

import com.example.huellitasVet.dto.Solicitudes.Persona;
import com.example.huellitasVet.model.Propietario;
import com.example.huellitasVet.service.PropietarioService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/propietarios")
public class PropietarioRestController {
    private final PropietarioService propietarios;

    @GetMapping
    public List<Propietario> listar(@RequestParam(defaultValue = "") String buscar) {
        return propietarios.buscar(buscar);
    }

    @GetMapping("/{id}")
    public Propietario detalle(@PathVariable Long id) {
        return propietarios.obtenerPorId(id).orElseThrow(() -> ErroresApi.ausente("Propietario"));
    }

    private Propietario guardar(Propietario p, Persona datos) {
        p.setNombres(datos.nombres().trim());
        p.setApellidos(datos.apellidos().trim());
        p.setTelefono(datos.telefono().trim());
        p.setCorreo(datos.correo().trim());
        return propietarios.guardar(p);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Propietario crear(@Valid @RequestBody Persona datos) {
        return guardar(new Propietario(), datos);
    }

    @PutMapping("/{id}")
    public Propietario editar(@PathVariable Long id, @Valid @RequestBody Persona datos) {
        return guardar(detalle(id), datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        detalle(id);
        propietarios.eliminar(id);
    }
}
