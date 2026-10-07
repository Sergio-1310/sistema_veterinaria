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
    public CommandLineRunner initData(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String emailAdmin = "admin@admin.com";
            
            if (usuarioRepository.findByEmail(emailAdmin).isEmpty()) {
                
                Usuario admin = new Usuario();
                admin.setNombre("Administrador");
                admin.setEmail(emailAdmin);
                admin.setRol("ADMIN");
                
                String passwordPlana = "admin";
                String passwordEncriptada = passwordEncoder.encode(passwordPlana);
                
                admin.setPassword(passwordEncriptada);

                usuarioRepository.save(admin);
                
            } else {
                System.out.println("Usuario ya creado");
            }
        };
    }
}