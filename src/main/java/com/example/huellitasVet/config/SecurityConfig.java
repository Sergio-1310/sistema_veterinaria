package com.example.huellitasVet.config;

import com.example.huellitasVet.security.JwtAuthenticationFilter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain cadena(HttpSecurity http, JwtAuthenticationFilter jwt) throws Exception {
        String[] personal = {"ADMIN", "RECEPCIONISTA", "VETERINARIO"};
        return http.cors(c -> {})
                .csrf(c -> c.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(c -> c.disable())
                .exceptionHandling(
                        e ->
                                e.authenticationEntryPoint(
                                                (req, res, ex) -> {
                                                    res.setStatus(401);
                                                    res.setContentType(
                                                            "application/json;charset=UTF-8");
                                                    res.getWriter()
                                                            .write(
                                                                    "{\"mensaje\":\"Inicia sesión"
                                                                        + " para continuar.\"}");
                                                })
                                        .accessDeniedHandler(
                                                (req, res, ex) -> {
                                                    res.setStatus(403);
                                                    res.setContentType(
                                                            "application/json;charset=UTF-8");
                                                    res.getWriter()
                                                            .write(
                                                                    "{\"mensaje\":\"No tienes"
                                                                        + " permiso para esta"
                                                                        + " operación.\"}");
                                                }))
                .authorizeHttpRequests(
                        a ->
                                a.dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR)
                                        .permitAll()
                                        .requestMatchers(HttpMethod.POST, "/api/auth/login")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.GET, "/", "/login")
                                        .permitAll()
                                        .requestMatchers("/api/usuarios", "/api/usuarios/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers(
                                                "/api/historial",
                                                "/api/historial/**",
                                                "/api/citas/*/atencion")
                                        .hasAnyRole("ADMIN", "VETERINARIO")
                                        .requestMatchers(HttpMethod.GET, "/api/**")
                                        .hasAnyRole(personal)
                                        .requestMatchers(
                                                "/api/veterinarios", "/api/veterinarios/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers(
                                                "/api/propietarios/**",
                                                "/api/mascotas/**",
                                                "/api/citas/**")
                                        .hasAnyRole("ADMIN", "RECEPCIONISTA")
                                        .anyRequest()
                                        .denyAll())
                .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.angular-origin}") String origen) {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(origen));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
