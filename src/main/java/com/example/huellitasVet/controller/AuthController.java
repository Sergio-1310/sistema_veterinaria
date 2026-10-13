package com.example.huellitasVet.controller;

import com.example.huellitasVet.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @GetMapping("/login")
    public String loginView() {
        return "login/login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String email,
            @RequestParam String password,
            HttpServletResponse response) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));

            // Generar el token
            String rol = auth.getAuthorities().iterator().next().getAuthority();
            String token = jwtUtil.generarToken(email, rol);

            // Guardar el token en una Cookie HttpOnly
            Cookie jwtCookie = new Cookie("jwt_token", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(86400); // 1 día de duración
            response.addCookie(jwtCookie);

            return "redirect:/propietarios";
        } catch (Exception e) {
            return "redirect:/login?error=true";
        }
    }

    @PostMapping("/auth/logout")
    public String logout(HttpServletResponse response) {

        // 1. Limpiar el contexto de seguridad de Spring de inmediato
        SecurityContextHolder.clearContext();

        Cookie jwtCookie = new Cookie("jwt_token", "");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/"); // Mismo path con el que se creó
        jwtCookie.setMaxAge(0); // 0 segundos = eliminación inmediata

        response.addCookie(jwtCookie);

        return "redirect:/login";
    }
}