package com.example.huellitasVet.security;

import com.example.huellitasVet.model.Usuario;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {
    private final Key clave;

    public JwtUtil(@Value("${app.jwt.secret}") String secreto) {
        clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secreto));
    }

    public String emitir(Usuario usuario) {
        var ahora = Instant.now();
        return Jwts.builder()
                .setSubject(usuario.getId().toString())
                .claim("v", usuario.getVersionSesion())
                .setIssuedAt(Date.from(ahora))
                .setExpiration(Date.from(ahora.plusSeconds(3600)))
                .signWith(clave)
                .compact();
    }

    public Claims verificar(String token) {
        return Jwts.parserBuilder().setSigningKey(clave).build().parseClaimsJws(token).getBody();
    }
}
