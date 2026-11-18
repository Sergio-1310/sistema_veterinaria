# HuellitasVet

Aplicación de gestión interna de una clínica veterinaria. El Avance 3 conecta un frontend Angular 22 con una API Spring Boot 4.1, Java 21, JPA y MySQL. La recepción vincula pacientes con propietarios y programa citas. El veterinario registra la atención y el administrador gestiona el equipo y sus accesos. El portal público de clientes del alcance preliminar queda fuera de este avance.

## Inicio en Windows y VS Code

Abre la carpeta que contiene `pom.xml` y `frontend/`. Se necesita un JDK 21, Node.js compatible con Angular 22 (se verificó con 24.22.1), npm y MySQL en ejecución. El proyecto incluye el wrapper de Maven. Usa dos terminales PowerShell desde la raíz.

**Terminal 1 · API**

```powershell
.\scripts\iniciar-api.ps1
```

El script utiliza la configuración local en `%LOCALAPPDATA%\HuellitasVet\entorno.xml`. Windows cifra la contraseña de MySQL y el secreto JWT para el usuario actual mediante DPAPI. En una instalación nueva solicita las credenciales y genera una clave aleatoria de 32 bytes. Para actualizar esa configuración ejecuta `.\scripts\iniciar-api.ps1 -Configurar`. La rotación de clave invalida los tokens emitidos previamente.

**Terminal 2 · Angular**

```powershell
cd frontend
npm.cmd ci
npm.cmd start
```

Abre [HuellitasVet en localhost](http://localhost:4200). Las peticiones a `/api` pasan por el proxy de Angular hacia el puerto 8080. Para otro backend configura `HV_API_TARGET` al iniciar Angular. El despliegue del build de producción requiere servir `dist/frontend/browser`, redirigir las rutas de Angular a `index.html` y enviar `/api/**` al backend.

### Configuración directa del backend

También puedes definir `DB_USER`, `DB_PASSWORD` y `JWT_SECRET` en el entorno de la terminal y ejecutar `.\mvnw.cmd spring-boot:run`. La clave JWT se facilita en Base64 y debe decodificarse a 32 bytes como mínimo. El inicio por script respeta las variables ya definidas.

- `DB_URL` usa por defecto `jdbc:mysql://localhost:3306/huellitas_vet`.
- `ANGULAR_ORIGIN` controla el origen admitido por CORS y la redirección de la raíz, con valor inicial `http://localhost:4200`.
- `SERVER_PORT` y `SERVER_ADDRESS` controlan la escucha HTTP. La configuración de desarrollo usa el puerto 8080 en la interfaz local.

## Acceso y roles

Inicia sesión con una cuenta activa existente. En una tabla de usuarios vacía, el perfil explícito `bootstrap` permite crear el primer administrador con `ADMIN_EMAIL` y `ADMIN_PASSWORD`. El inicio normal conserva las cuentas existentes. Las nuevas contraseñas requieren al menos 10 caracteres y admiten hasta 72 bytes.

- **ADMIN** gestiona todos los módulos, incluidas las cuentas, los roles, los cambios de contraseña y los estados de activación.
- **RECEPCIONISTA** mantiene propietarios y pacientes, consulta el equipo médico y programa, edita o cancela citas.
- **VETERINARIO** consulta la agenda y el historial clínico. Puede finalizar una cita asignada al profesional cuyo correo coincide con el de su cuenta. Al incorporar un veterinario, registra el profesional y crea su cuenta con el mismo correo.

Spring Security valida el rol en cada petición. La API emite tokens Bearer de una hora y verifica la firma, la expiración, la existencia, el estado y la versión de la cuenta. Angular conserva el token en `sessionStorage` de la pestaña, recupera el perfil después de recargar y elimina el token al cerrar sesión. Los cambios en la cuenta invalidan las sesiones anteriores. La API recibe el token en la cabecera `Authorization` y prescinde de cookies de autenticación.

## Flujos de trabajo

1. Desde **Propietarios**, abre una ficha y elige **Registrar mascota**. El formulario preselecciona al propietario. En la ficha del paciente puedes revisar sus citas y, con permiso clínico, sus atenciones.
2. Desde **Citas** o la ficha de una mascota, elige **Programar cita**. Selecciona un veterinario activo, un horario futuro y el motivo. Una cita programada se puede editar o cancelar con confirmación. El backend serializa las reservas del mismo veterinario y rechaza conflictos de horario.
3. Desde una cita programada, **Registrar atención** permite introducir diagnóstico, tratamiento y observaciones. Una única transacción guarda la ficha y marca la cita como atendida. La restricción única de cita impide duplicar atenciones.
4. **Inicio** muestra el número de pacientes, las citas programadas para hoy, las pendientes y las seis próximas citas. Los indicadores se calculan desde MySQL al entrar.
5. Desde **Usuarios**, el administrador puede editar roles, cambiar la contraseña y activar o desactivar cuentas. La desactivación impide usar tokens anteriores y volver a iniciar sesión.

## Persistencia y migración del Avance 2

El script versionado `db/migraciones/V3__agenda_clinica.sql` amplía las tablas `mascota`, `cita` y `usuarios` del Avance 2 y agrega `atenciones`. Las relaciones se protegen con claves foráneas. Se conservan los identificadores, la edad anterior, el nombre anterior del propietario y las cadenas originales de fecha y hora de las citas. Las consultas clínicas obtienen la mascota, el veterinario y el motivo mediante su cita, que queda cerrada después de la atención.

**En la instalación local de esta entrega, V3 ya está aplicada.** La aplicación arranca con `ddl-auto=validate` y `spring.sql.init.mode=never`. Para migrar otra instalación del Avance 2:

1. Verifica el esquema y realiza un respaldo completo con MySQL. Restaura ese respaldo en una base aislada y compara los datos antes de tocar el original.
2. Ensaya V3 en la copia. En MySQL Workbench selecciona el esquema de destino, abre el archivo SQL y ejecuta el script completo una sola vez. Las operaciones DDL de MySQL confirman cada cambio de esquema. Si se interrumpe el script, inspecciona las columnas, índices y restricciones ya aplicados antes de continuar con las instrucciones pendientes.
3. Compara los recuentos e identificadores y revisa los vínculos pendientes. Aplica la misma migración al original respaldado en una ventana sin escrituras.

Un nombre completo con una única coincidencia se vincula al propietario. Una coincidencia ambigua queda pendiente para que recepción identifique a la persona correcta desde **Mascotas → Editar**. Las fechas válidas pasan a columnas DATE/TIME. El historial previo a la migración no permite deducir veterinarios, diagnósticos ni tratamientos. Se conservan los valores de origen.

```sql
SELECT id, nombre, nombre_propietario FROM mascota WHERE propietario_id IS NULL;
SELECT id, mascota_id, fecha, hora, estado FROM cita
WHERE paciente_id IS NULL OR veterinario_id IS NULL
   OR fecha_programada IS NULL OR hora_programada IS NULL
   OR estado IS NULL OR estado NOT IN ('PROGRAMADA','ATENDIDA','CANCELADA');
```

## Organización y API

- `src/main/java/com/example/huellitasVet/` contiene las entidades y repositorios JPA, servicios, solicitudes y respuestas, controladores REST, seguridad y configuración.
- `frontend/src/app/core/` contiene la sesión, el interceptor, los modelos TypeScript y el servicio HTTP. `features/` implementa el inicio, el dashboard, los listados, las fichas y los formularios. `shared/` contiene la tabla y los iconos reutilizables. `src/styles.scss` define los estilos globales.
- `frontend/src/app/app.routes.ts` define las rutas con carga diferida, los parámetros, el acceso por rol, el acceso denegado y la página no encontrada.

| Recurso | Operaciones |
| --- | --- |
| `/api/auth` | `POST /login` con email y password, `GET /me` con Bearer |
| `/api/propietarios` | Listar con `buscar`, crear, consultar, editar y eliminar por ID si no tiene mascotas vinculadas |
| `/api/mascotas` | Listar con `q`, `especie` o `propietarioId`, crear, consultar, editar y eliminar si no tiene citas |
| `/api/veterinarios` | Listar con `q` y `activo`, crear, consultar, editar y `PATCH /{id}/activo` |
| `/api/citas` | Listar con `q`, `fecha`, `estado`, `mascotaId` o `veterinarioId`, crear, consultar, editar y cancelar con `DELETE /{id}` |
| `/api/citas/{id}/atencion` | `POST` con diagnóstico, tratamiento y observaciones |
| `/api/historial` | Listar con `q` o `mascotaId` y consultar por ID |
| `/api/usuarios` | Listar, crear, consultar, editar, `PATCH /{id}/activo` y `PUT /{id}/password` |
| `/api/dashboard` | Resumen y próximas citas |

Los listados y detalles usan GET. Las altas usan POST y devuelven 201. Las ediciones usan PUT. Las operaciones de activación reciben un objeto con el booleano `activo`. Los errores de solicitud devuelven `mensaje` y, cuando corresponde, `campos`. Un 401 indica que se requiere iniciar sesión, un 403 indica acceso insuficiente y un 409 indica un conflicto de negocio o integridad.

## Verificación

Desde la raíz:

```powershell
.\mvnw.cmd clean package
cd frontend
npm.cmd ci
npm.cmd run build
```

La prueba Maven arranca el servidor HTTP con H2 en memoria. Recorre las tres cuentas y comprueba validación, duplicados, protección de registros relacionados, cambios de estado, historial, restricción de rutas y revocación de sesiones.

La prueba Playwright en `frontend/e2e/clinica.spec.ts` usa navegador real y crea registros de prueba. Para repetirla, inicia una **copia MySQL de pruebas** ya migrada, configura `DB_URL` para esa copia y `SERVER_PORT=18083` en la terminal del backend. En otra terminal, desde `frontend`, define `E2E_EMAIL` y `E2E_PASSWORD` de un administrador de esa copia. La prueba crea cuentas clínicas exclusivas de esa ejecución y arranca su propio servidor Angular en el puerto 14203.

```powershell
$env:PW_CHANNEL = 'msedge'
npm.cmd test
```

El comando utiliza Microsoft Edge instalado. Con Chromium de Playwright, instala el navegador con `npx.cmd playwright install chromium` y usa el canal `chromium`. Las pruebas se ejecutan contra una base separada de la clínica.
Para comprobar persistencia, toma el ID de la ficha creada en el primer recorrido, reinicia el backend de pruebas y define `E2E_FICHA_ANTERIOR` con ese ID antes de repetir `npm.cmd test`. La segunda prueba inicia una sesión nueva, abre esa misma ficha y verifica el estado de su cita.

### Resultados de esta entrega

- Maven empaquetó el backend con Java 21. La prueba HTTP integrada pasó con 0 fallos y 0 errores.
- Angular 22.2.6 compiló en producción con validación estricta de TypeScript y plantillas.
- Edge completó los dos escenarios Playwright contra la copia MySQL, incluido el acceso a la atención previa después de reiniciar ambos servidores.
- En la base local se conservaron los 3 propietarios, 1 mascota, 2 veterinarios y 2 usuarios originales. La agenda estaba vacía antes de la migración. Una restauración aislada y la comparación de sus datos verificaron el respaldo.
- La mascota heredada con ID 1 requiere identificar al propietario en **Mascotas → Editar**. La cuenta veterinaria anterior necesita que un administrador confirme a qué profesional corresponde y alinee su correo en **Usuarios** o **Veterinarios** antes de registrar atenciones con esa cuenta.
