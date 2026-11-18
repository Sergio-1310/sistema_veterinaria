package com.example.huellitasVet.model;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "mascota")
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "especie")
    private String especie;

    @Column(name = "raza")
    private String raza;

    @Column(name = "edad")
    private Integer edad;

    @Column(name = "nombre_propietario")
    private String nombrePropietario;

    private java.time.LocalDate fechaNacimiento;
    private String sexo;

    @ManyToOne
    @JoinColumn(name = "propietario_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Propietario propietario;

    public Long getPropietarioId() {
        return propietario == null ? null : propietario.getId();
    }

    public String getPropietarioNombre() {
        return propietario == null
                ? nombrePropietario
                : propietario.getNombres() + " " + propietario.getApellidos();
    }

    public Mascota() {}
}
