package com.example.huellitasVet.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "veterinarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Veterinario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(length = 100)
    private String especialidad; // Ej: Medicina General, Cirugía, etc.

    @Column(length = 20)
    private String telefono;

    @Column(nullable = false, unique = true, length = 120)
    private String correo;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true; // Por defecto activo

    // Helper para iniciales (ej. AF, VC)
    public String getIniciales() {
        String n = (nombres != null && !nombres.isBlank()) ? nombres.trim().substring(0, 1).toUpperCase() : "";
        String a = (apellidos != null && !apellidos.isBlank()) ? apellidos.trim().substring(0, 1).toUpperCase() : "";
        return n + a;
    }

    public String getNombreCompletoConTitulo() {
        return "Dr. " + (nombres != null ? nombres : "") + " " + (apellidos != null ? apellidos : "");
    }
}