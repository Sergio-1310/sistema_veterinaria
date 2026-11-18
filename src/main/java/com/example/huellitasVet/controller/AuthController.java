package com.example.huellitasVet.controller;

import com.example.huellitasVet.dto.Solicitudes;
import com.example.huellitasVet.model.Usuario;
import com.example.huellitasVet.repository.UsuarioRepository;
import com.example.huellitasVet.security.JwtUtil;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager manager;
    private final UsuarioRepository usuarios;
    private final JwtUtil jwt;

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody Solicitudes.Login datos) {
        try {
            var usuario = usuarios.findByEmail(datos.email().trim()).orElse(null);
            manager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            datos.email().trim(), datos.password()));
            if (usuario == null || !Boolean.TRUE.equals(usuario.getActivo()))
                throw new BadCredentialsException("Cuenta no disponible");
            return Map.of("token", jwt.emitir(usuario), "usuario", usuario);
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos.");
        }
    }

    @GetMapping("/me")
    public Usuario perfil(Authentication auth) {
        return usuarios.findByEmail(auth.getName())
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED, "Sesión no válida."));
    }
}
