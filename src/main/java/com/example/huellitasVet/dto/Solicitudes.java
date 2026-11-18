package com.example.huellitasVet.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

public final class Solicitudes {
    private Solicitudes() {}

    public static final String TELEFONO = "(?=(?:[^0-9]*[0-9]){7})[+0-9() -]{7,20}";

    public record Login(
            @NotBlank @Email String email, @NotBlank @Size(max = 128) String password) {}

    public record Persona(
            @NotBlank @Size(max = 100) String nombres,
            @NotBlank @Size(max = 100) String apellidos,
            @NotBlank @Pattern(regexp = TELEFONO) String telefono,
            @NotBlank @Email @Size(max = 100) String correo) {}

    public record Medico(
            @NotBlank @Size(max = 100) String nombres,
            @NotBlank @Size(max = 100) String apellidos,
            @Pattern(regexp = TELEFONO) String telefono,
            @NotBlank @Email @Size(max = 120) String correo,
            @Size(max = 100) String especialidad) {}

    public record Paciente(
            @NotBlank @Size(max = 100) String nombre,
            @NotBlank @Size(max = 80) String especie,
            @Size(max = 100) String raza,
            @PastOrPresent LocalDate fechaNacimiento,
            @Pattern(regexp = "Macho|Hembra|Desconocido") String sexo,
            @NotNull @Positive Long propietarioId) {}

    public record Turno(
            @NotNull @Positive Long mascotaId,
            @NotNull @Positive Long veterinarioId,
            @NotNull LocalDate fecha,
            @NotNull LocalTime hora,
            @NotBlank @Size(max = 255) String motivo) {}

    public record Consulta(
            @NotBlank @Size(max = 2000) String diagnostico,
            @NotBlank @Size(max = 2000) String tratamiento,
            @Size(max = 2000) String observaciones) {}

    public record Cuenta(
            @NotBlank @Size(max = 100) String nombre,
            @NotBlank @Email @Size(max = 100) String email,
            @NotBlank @Pattern(regexp = "ADMIN|RECEPCIONISTA|VETERINARIO") String rol,
            @Size(min = 10, max = 72) String password) {}

    public record Clave(@NotBlank @Size(min = 10, max = 72) String password) {}

    public record Activacion(@NotNull Boolean activo) {}
}
