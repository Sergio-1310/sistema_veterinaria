package com.example.huellitasVet.security;

import com.example.huellitasVet.repository.UsuarioRepository;

import io.jsonwebtoken.JwtException;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwt;
    private final UsuarioRepository usuarios;

    @Override
    protected void doFilterInternal(
            HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        var header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                var claims = jwt.verificar(header.substring(7));
                var version = claims.get("v", Long.class);
                usuarios.findById(Long.valueOf(claims.getSubject()))
                        .filter(
                                u ->
                                        Boolean.TRUE.equals(u.getActivo())
                                                && version != null
                                                && u.getVersionSesion() == version)
                        .ifPresent(
                                u ->
                                        SecurityContextHolder.getContext()
                                                .setAuthentication(
                                                        new UsernamePasswordAuthenticationToken(
                                                                u.getEmail(),
                                                                null,
                                                                List.of(
                                                                        new SimpleGrantedAuthority(
                                                                                "ROLE_"
                                                                                        + u
                                                                                                .getRol())))));
            } catch (JwtException | IllegalArgumentException ex) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(req, res);
    }
}
