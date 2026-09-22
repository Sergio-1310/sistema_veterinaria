INSERT INTO mascota
(nombre, especie, raza, edad, nombre_propietario)
SELECT 'Max', 'Perro', 'Labrador', 4, 'Carlos Pérez'
WHERE NOT EXISTS (
    SELECT 1
    FROM mascota
    WHERE nombre = 'Max'
      AND nombre_propietario = 'Carlos Pérez'
);