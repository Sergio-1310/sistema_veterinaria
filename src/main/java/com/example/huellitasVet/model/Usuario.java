package com.example.huellitasVet.model;

import jakarta.persistence.*;

import lombok.Data;

@Entity
@Table(name = "usuarios")
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    @com.fasterxml.jackson.annotation.JsonIgnore
    @lombok.ToString.Exclude
    private String password;

    @com.fasterxml.jackson.annotation.JsonIgnore @Version private long versionSesion;

    @Column(nullable = false, length = 20)
    private String rol;

    private Boolean activo = true;
}
