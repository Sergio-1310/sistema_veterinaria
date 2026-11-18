-- Instalación nueva en MySQL 8.0 o superior. Ejecutar una vez en una base vacía.
CREATE DATABASE IF NOT EXISTS huellitas_vet CHARACTER SET utf8mb4;
USE huellitas_vet;

CREATE TABLE propietarios (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    correo VARCHAR(100) NOT NULL,
    UNIQUE KEY uk_propietarios_correo (correo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE veterinarios (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    especialidad VARCHAR(100),
    telefono VARCHAR(20),
    correo VARCHAR(120) NOT NULL,
    activo BIT(1) NOT NULL,
    UNIQUE KEY uk_veterinarios_correo (correo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL,
    activo BIT(1),
    version_sesion BIGINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_usuarios_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE mascota (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255),
    especie VARCHAR(255),
    raza VARCHAR(255),
    edad INT,
    nombre_propietario VARCHAR(255),
    fecha_nacimiento DATE,
    sexo VARCHAR(255),
    propietario_id BIGINT,
    CONSTRAINT fk_mascota_propietario FOREIGN KEY (propietario_id) REFERENCES propietarios(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE cita (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    estado VARCHAR(255),
    fecha VARCHAR(255),
    hora VARCHAR(255),
    mascota_id BIGINT,
    motivo VARCHAR(255),
    paciente_id BIGINT,
    veterinario_id BIGINT,
    fecha_programada DATE,
    hora_programada TIME,
    KEY ix_agenda (veterinario_id, fecha_programada, hora_programada, estado),
    KEY ix_cita_fecha (estado, fecha_programada, hora_programada),
    CONSTRAINT fk_cita_paciente FOREIGN KEY (paciente_id) REFERENCES mascota(id),
    CONSTRAINT fk_cita_veterinario FOREIGN KEY (veterinario_id) REFERENCES veterinarios(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE atenciones (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    cita_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    diagnostico VARCHAR(2000) NOT NULL,
    tratamiento VARCHAR(2000) NOT NULL,
    observaciones VARCHAR(2000),
    UNIQUE KEY uk_atenciones_cita (cita_id),
    CONSTRAINT fk_atencion_cita FOREIGN KEY (cita_id) REFERENCES cita(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
