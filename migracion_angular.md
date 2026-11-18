Nuevas funcionalidades y mejoras

Cambios implementados

1. Migración del frontend a Angular 22

Se implementó una nueva interfaz utilizando Angular 22, TypeScript y SCSS, reemplazando las vistas anteriores de Thymeleaf. Se incorporaron componentes reutilizables, navegación mediante rutas, formularios reactivos y diseño adaptable.

2. Dashboard

Se implementó un panel principal con estadísticas obtenidas desde el backend, incluyendo mascotas registradas, citas programadas, citas pendientes y próximas atenciones.

3. Gestión de propietarios

Se implementó el registro, consulta, búsqueda, edición y eliminación controlada de propietarios, incluyendo la asociación con sus mascotas.

4. Gestión de mascotas

Se completó el registro, edición, búsqueda y consulta de mascotas, incorporando su vinculación con propietarios y el acceso a sus citas e historial clínico.

5. Gestión de veterinarios

Se implementó la administración de profesionales veterinarios, incluyendo registro, búsqueda, edición, especialidades y activación o desactivación.

6. Gestión de citas

Se implementó la programación, consulta, edición, filtrado y cancelación de citas veterinarias. El backend valida la disponibilidad del profesional, evita conflictos de horarios y controla los estados de las citas.

7. Atenciones e historial clínico

Se incorporó el registro de diagnóstico, tratamiento y observaciones. Al finalizar una atención, se guarda la ficha clínica y se actualiza automáticamente el estado de la cita a ATENDIDA mediante una transacción.

8. Administración de usuarios

Se implementó la gestión de usuarios, roles, activación, desactivación y cambio de contraseñas, con permisos diferenciados para ADMIN, RECEPCIONISTA y VETERINARIO.

9. Autenticación y seguridad

Se integró Spring Security con JWT Bearer y BCrypt. Se implementaron rutas protegidas, interceptores HTTP, validación de sesiones y restricciones de acceso tanto en Angular como en Spring Boot.

10. Validaciones e integración REST

Se incorporaron formularios reactivos, validaciones en frontend y backend, manejo de errores y comunicación mediante HttpClient con los endpoints REST de Spring Boot.
