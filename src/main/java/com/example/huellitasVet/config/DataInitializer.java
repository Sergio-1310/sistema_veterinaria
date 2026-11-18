package com.example.huellitasVet.config;

import com.example.huellitasVet.model.Usuario;
import com.example.huellitasVet.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("bootstrap")
public class DataInitializer {
    @Bean
    CommandLineRunner primerAdministrador(
            UsuarioRepository usuarios,
            PasswordEncoder encoder,
            @Value("${ADMIN_EMAIL}") String email,
            @Value("${ADMIN_PASSWORD}") String clave) {
        return args -> {
            if (usuarios.count() != 0) return;
            if (clave.isBlank()
                    || clave.length() < 10
                    || clave.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
                throw new IllegalArgumentException(
                        "La clave inicial debe tener al menos 10 caracteres y máximo 72 bytes.");
            var usuario = new Usuario();
            usuario.setNombre("Administración");
            usuario.setRol("ADMIN");
            usuario.setEmail(email.trim());
            usuario.setPassword(encoder.encode(clave));
            usuarios.save(usuario);
        };
    }
}
