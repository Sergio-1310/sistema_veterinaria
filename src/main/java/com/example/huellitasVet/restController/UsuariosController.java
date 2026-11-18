package com.example.huellitasVet.restController;

import static com.example.huellitasVet.restController.ErroresApi.*;

import com.example.huellitasVet.dto.Solicitudes.*;
import com.example.huellitasVet.model.Usuario;
import com.example.huellitasVet.repository.UsuarioRepository;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Transactional
public class UsuariosController {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    @GetMapping
    public List<Usuario> listar() {
        return usuarios.findAll();
    }

    @GetMapping("/{id}")
    public Usuario detalle(@PathVariable Long id) {
        return usuarios.findById(id).orElseThrow(() -> ausente("Usuario"));
    }

    private void actualizar(Usuario u, Cuenta datos) {
        u.setNombre(datos.nombre().trim());
        u.setEmail(datos.email().trim());
        u.setRol(datos.rol());
    }

    private void clave(Usuario u, String valor) {
        if (valor == null
                || valor.isBlank()
                || valor.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña es obligatoria y admite hasta 72 bytes.");
        u.setPassword(encoder.encode(valor));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario crear(@Valid @RequestBody Cuenta datos) {
        var u = new Usuario();
        actualizar(u, datos);
        clave(u, datos.password());
        return usuarios.save(u);
    }

    @PutMapping("/{id}")
    public Usuario editar(
            @PathVariable Long id, @Valid @RequestBody Cuenta datos, Principal actor) {
        var u = detalle(id);
        if (u.getEmail().equals(actor.getName()) && !"ADMIN".equals(datos.rol()))
            throw conflicto("No puedes retirar tu propio rol de administrador.");
        actualizar(u, datos);
        return u;
    }

    @PatchMapping("/{id}/activo")
    public Usuario activar(
            @PathVariable Long id, @Valid @RequestBody Activacion datos, Principal actor) {
        var u = detalle(id);
        if (u.getEmail().equals(actor.getName()) && !datos.activo())
            throw conflicto("No puedes desactivar tu propia cuenta.");
        u.setActivo(datos.activo());
        return u;
    }

    @PutMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarClave(@PathVariable Long id, @Valid @RequestBody Clave datos) {
        clave(detalle(id), datos.password());
    }
}
