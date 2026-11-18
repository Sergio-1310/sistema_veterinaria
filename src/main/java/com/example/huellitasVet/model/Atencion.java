package com.example.huellitasVet.model;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "atenciones")
public class Atencion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "cita_id", unique = true)
    private Cita cita;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 2000)
    private String diagnostico;

    @Column(nullable = false, length = 2000)
    private String tratamiento;

    @Column(length = 2000)
    private String observaciones;
}
