-- Datos ficticios para una instalación nueva. Las cuentas de acceso se crean en la aplicación.
USE huellitas_vet;
START TRANSACTION;
SET @cargar_demo = NOT EXISTS (SELECT 1 FROM propietarios)
    AND NOT EXISTS (SELECT 1 FROM veterinarios)
    AND NOT EXISTS (SELECT 1 FROM mascota)
    AND NOT EXISTS (SELECT 1 FROM cita)
    AND NOT EXISTS (SELECT 1 FROM atenciones);

INSERT INTO propietarios (nombres, apellidos, telefono, correo)
SELECT 'Alba', 'Ejemplo', '000000001', 'alba@example.invalid' WHERE @cargar_demo
UNION ALL SELECT 'Nico', 'Ejemplo', '000000002', 'nico@example.invalid' WHERE @cargar_demo;

INSERT INTO veterinarios (nombres, apellidos, especialidad, telefono, correo, activo)
SELECT 'Mar', 'Demo', 'Medicina general', '000000003', 'mar@example.invalid', 1 WHERE @cargar_demo
UNION ALL SELECT 'Sol', 'Demo', 'Dermatología', '000000004', 'sol@example.invalid', 1 WHERE @cargar_demo;

INSERT INTO mascota (nombre, especie, raza, sexo, fecha_nacimiento, propietario_id)
SELECT 'Nube', 'Gato', 'Mestiza', 'Hembra', CURRENT_DATE - INTERVAL 2 YEAR, id
FROM propietarios WHERE @cargar_demo AND correo = 'alba@example.invalid'
UNION ALL
SELECT 'Milo', 'Perro', 'Mestizo', 'Macho', CURRENT_DATE - INTERVAL 3 YEAR, id
FROM propietarios WHERE @cargar_demo AND correo = 'nico@example.invalid';

INSERT INTO cita (paciente_id, veterinario_id, fecha_programada, hora_programada, motivo, estado)
SELECT m.id, v.id, CURRENT_DATE + INTERVAL 1 DAY, '09:00:00', 'Control anual', 'PROGRAMADA'
FROM mascota m JOIN propietarios p ON p.id = m.propietario_id
JOIN veterinarios v ON v.correo = 'mar@example.invalid'
WHERE @cargar_demo AND p.correo = 'alba@example.invalid' AND m.nombre = 'Nube'
UNION ALL
SELECT m.id, v.id, CURRENT_DATE + INTERVAL 2 DAY, '11:30:00', 'Revisión de piel', 'PROGRAMADA'
FROM mascota m JOIN propietarios p ON p.id = m.propietario_id
JOIN veterinarios v ON v.correo = 'sol@example.invalid'
WHERE @cargar_demo AND p.correo = 'nico@example.invalid' AND m.nombre = 'Milo';
COMMIT;
SELECT IF(@cargar_demo, 'Demo cargada.', 'La clínica ya tenía registros. Se omitió la demo.') AS resultado;
