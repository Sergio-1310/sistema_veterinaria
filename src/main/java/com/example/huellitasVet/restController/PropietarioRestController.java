package com.example.huellitasVet.restController;

import com.example.huellitasVet.model.Propietario;
import com.example.huellitasVet.service.PropietarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/propietarios")
@RequiredArgsConstructor
public class PropietarioRestController {

    private final PropietarioService propietarioService;

    @GetMapping
    public ResponseEntity<List<Propietario>> listarPropietarios(@RequestParam(required = false) String buscar) {
        List<Propietario> propietarios = propietarioService.buscar(buscar);
        return ResponseEntity.ok(propietarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        return propietarioService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> guardarPropietario(@RequestBody Propietario propietario) {
        try {
            Propietario nuevoPropietario = propietarioService.guardar(propietario);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPropietario);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarPropietario(@PathVariable Long id, @RequestBody Propietario propietario) {
        return propietarioService.obtenerPorId(id).map(existente -> {
            existente.setNombres(propietario.getNombres());
            existente.setApellidos(propietario.getApellidos());
            existente.setTelefono(propietario.getTelefono());
            existente.setCorreo(propietario.getCorreo());
            Propietario actualizado = propietarioService.guardar(existente);
            return ResponseEntity.ok(actualizado);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPropietario(@PathVariable Long id) {
        if (propietarioService.obtenerPorId(id).isPresent()) {
            propietarioService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}