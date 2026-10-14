package com.example.huellitasVet.config;

import com.example.huellitasVet.model.Usuario;
import com.example.huellitasVet.repository.UsuarioRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // ADMIN
            String emailAdmin = "admin@admin.com";

            if (usuarioRepository.findByEmail(emailAdmin).isEmpty()) {
                Usuario admin = new Usuario();
                admin.setNombre("Administrador");
                admin.setEmail(emailAdmin);
                admin.setRol("ADMIN");
                admin.setPassword(passwordEncoder.encode("admin"));

                usuarioRepository.save(admin);
            }

            // VETERINARIO
            String emailVeterinario = "veterinario@huellitas.com";

            if (usuarioRepository.findByEmail(emailVeterinario).isEmpty()) {
                Usuario veterinario = new Usuario();
                veterinario.setNombre("Veterinario");
                veterinario.setEmail(emailVeterinario);
                veterinario.setRol("VETERINARIO");
                veterinario.setPassword(
                        passwordEncoder.encode("vet123"));

                usuarioRepository.save(veterinario);
            }
        };
    }
}