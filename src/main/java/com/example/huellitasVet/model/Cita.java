package com.example.huellitasVet.model;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "cita")
public class Cita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mascota_id")
    private Long mascotaIdAnterior;

    @Column(name = "fecha")
    private String fechaAnterior;

    @Column(name = "hora")
    private String horaAnterior;

    private String motivo;
    private String estado;
    private LocalDate fechaProgramada;
    private LocalTime horaProgramada;

    @ManyToOne
    @JoinColumn(name = "paciente_id")
    private Mascota mascota;

    @ManyToOne
    @JoinColumn(name = "veterinario_id")
    private Veterinario veterinario;
}
