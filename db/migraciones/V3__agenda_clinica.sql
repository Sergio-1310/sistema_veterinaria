-- MySQL 8.0+ / 9. Ejecutar una sola vez sobre un esquema de Avance 2 respaldado.
ALTER TABLE mascota
    ADD fecha_nacimiento DATE NULL,
    ADD sexo VARCHAR(255) NULL,
    ADD propietario_id BIGINT NULL,
    ADD CONSTRAINT fk_mascota_propietario FOREIGN KEY (propietario_id) REFERENCES propietarios(id);

ALTER TABLE cita
    ADD paciente_id BIGINT NULL,
    ADD veterinario_id BIGINT NULL,
    ADD fecha_programada DATE NULL,
    ADD hora_programada TIME NULL,
    ADD CONSTRAINT fk_cita_paciente FOREIGN KEY (paciente_id) REFERENCES mascota(id),
    ADD CONSTRAINT fk_cita_veterinario FOREIGN KEY (veterinario_id) REFERENCES veterinarios(id),
    ADD INDEX ix_agenda (veterinario_id, fecha_programada, hora_programada, estado),
    ADD INDEX ix_cita_fecha (estado, fecha_programada, hora_programada);

ALTER TABLE usuarios ADD version_sesion BIGINT NOT NULL DEFAULT 0;

CREATE TABLE atenciones (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cita_id BIGINT NOT NULL UNIQUE,
    fecha DATE NOT NULL,
    diagnostico VARCHAR(2000) NOT NULL,
    tratamiento VARCHAR(2000) NOT NULL,
    observaciones VARCHAR(2000),
    CONSTRAINT fk_atencion_cita FOREIGN KEY (cita_id) REFERENCES cita(id)
) ENGINE=InnoDB;

START TRANSACTION;
UPDATE mascota m JOIN (
    SELECT TRIM(CONCAT(nombres,' ',apellidos)) AS nombre, MIN(id) AS id
    FROM propietarios GROUP BY TRIM(CONCAT(nombres,' ',apellidos)) HAVING COUNT(*) = 1
) p ON TRIM(m.nombre_propietario) = p.nombre SET m.propietario_id = p.id;
UPDATE cita c JOIN mascota m ON m.id=c.mascota_id SET c.paciente_id=m.id;
UPDATE cita SET fecha_programada=STR_TO_DATE(fecha,'%Y-%m-%d')
WHERE fecha REGEXP '^[1-9][0-9]{3}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$'
  AND CAST(RIGHT(fecha,2) AS UNSIGNED) <= DAY(LAST_DAY(CONCAT(LEFT(fecha,7),'-01')));
UPDATE cita SET hora_programada=CAST(hora AS TIME)
WHERE hora REGEXP '^([01][0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$';
UPDATE cita SET estado='PROGRAMADA' WHERE UPPER(TRIM(estado)) IN ('PENDIENTE','PROGRAMADA','PROGRAMADO');
UPDATE cita SET estado='CANCELADA' WHERE UPPER(TRIM(estado)) IN ('CANCELADA','CANCELADO');
UPDATE cita SET estado='ATENDIDA' WHERE UPPER(TRIM(estado)) IN ('ATENDIDA','ATENDIDO','COMPLETADA');
COMMIT;
