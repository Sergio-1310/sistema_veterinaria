# HuellitasVet

Aplicación de gestión interna de una clínica veterinaria. El Avance 3 conecta un frontend Angular 22 con una API Spring Boot 4.1, Java 21, JPA y MySQL. La recepción vincula pacientes con propietarios y programa citas. El veterinario registra la atención y el administrador gestiona el equipo y sus accesos. El portal público de clientes del alcance preliminar queda fuera de este avance.

## Instalación nueva en Windows con VS Code

Necesitas Git, VS Code, JDK 21 (incluye `javac`), Node.js 24.15 o posterior de la serie 24 con npm, y MySQL Server 8.0 o superior con MySQL Workbench. Inicia el servicio MySQL local en el puerto 3306. Ten a mano tu usuario y contraseña de MySQL con acceso para crear la base y sus tablas. La primera instalación requiere conexión a Internet para Maven y npm.

### 1. Clonar y abrir el proyecto

Desde PowerShell:

```powershell
git clone --branch angular --single-branch https://github.com/Sergio-1310/sistema_veterinaria.git HuellitasVet
cd HuellitasVet
code .
```

En VS Code abre **Terminal → Nueva terminal**, selecciona PowerShell y comprueba los requisitos:

```powershell
git branch --show-current
javac -version
node --version
npm.cmd --version
```

La rama debe ser `angular` y el compilador Java debe indicar 21. Si Windows no reconoce `code`, abre VS Code y selecciona la carpeta clonada con **Archivo → Abrir carpeta**.

### 2. Crear la base de datos

1. En MySQL Workbench abre la conexión de tu computadora. Selecciona **File → Open SQL Script** y abre `db/instalacion/01_esquema.sql` desde la carpeta clonada.
2. Ejecuta el archivo completo con **Query → Execute (All or Selection)**, sin seleccionar texto. El script crea `huellitas_vet` y sus seis tablas vacías. En **Schemas**, pulsa actualizar y comprueba que aparece la base.
3. **Opcional:** abre y ejecuta `db/instalacion/02_demo_opcional.sql` del mismo modo. Agrega dos propietarios, dos mascotas, dos veterinarios y dos citas futuras de ejemplo. Se omite si la clínica ya tiene registros. Las cuentas de acceso se crean por separado.

Utiliza el esquema inicial una sola vez, en una instalación vacía. Si ya trabajabas con datos del Avance 2, sigue la sección **Persistencia y migración del Avance 2**. Si tu instalación ya tiene las seis tablas del Avance 3, continúa con el arranque normal.

### 3. Configurar MySQL y crear el primer administrador

Desde la terminal PowerShell de VS Code, en la carpeta con `pom.xml`:

```powershell
powershell.exe -NoProfile -ExecutionPolicy RemoteSigned -File .\scripts\iniciar-api.ps1 -CrearAdministrador
```

La primera vez, escribe el usuario y la contraseña **de MySQL de tu equipo**. A continuación, elige un correo y una contraseña **para el administrador de HuellitasVet** y confírmala. La entrada de contraseñas está oculta. La clave del administrador admite un mínimo de 10 caracteres y un máximo de 72 bytes UTF-8.

El script guarda la configuración de conexión en `%LOCALAPPDATA%\HuellitasVet\entorno.xml`. Windows cifra la contraseña de MySQL y la clave JWT para tu cuenta de Windows. Spring Boot guarda el hash BCrypt del administrador en MySQL. El perfil `bootstrap` crea la cuenta solo si `usuarios` está vacía. Espera el mensaje `Started HuellitasVetApplication` y deja la terminal abierta.

### 4. Iniciar Angular y entrar

Abre una **segunda terminal PowerShell** en la raíz del repositorio:

```powershell
cd frontend
npm.cmd ci
npm.cmd start
```

Abre [HuellitasVet](http://localhost:4200/login) e ingresa con el correo y la contraseña ADMIN que elegiste. Angular se conecta a la API local en el puerto 8080 mediante su proxy.

### Siguientes arranques

En la primera terminal ejecuta el siguiente comando desde la raíz. En la segunda, desde `frontend`, ejecuta `npm.cmd start`. Para detener un servidor pulsa **Ctrl+C** en su terminal.

```powershell
powershell.exe -NoProfile -ExecutionPolicy RemoteSigned -File .\scripts\iniciar-api.ps1
```

### Solución de problemas

- **Puerto 4200 ocupado.** Detén la ejecución anterior de Angular o ejecuta `npm.cmd start -- --port 4300` desde `frontend` y abre [el login en 4300](http://localhost:4300/login). El proxy usa el mismo origen que la página.
- **Puerto 8080 ocupado.** En la terminal de la API define `$env:SERVER_PORT = '8081'` antes de iniciarla. En la terminal de Angular define `$env:HV_API_TARGET = 'http://127.0.0.1:8081'` antes de `npm.cmd start`. Reinicia ambos si ya estaban abiertos.
- **MySQL rechaza la conexión.** Comprueba en **Servicios** de Windows que MySQL esté iniciado y prueba la conexión en Workbench. Si usa otro puerto, por ejemplo 3307, define `$env:DB_URL = 'jdbc:mysql://127.0.0.1:3307/huellitas_vet'` en la terminal de la API antes de arrancarla.
- **MySQL indica “Access denied”.** Verifica el usuario y la contraseña en Workbench. Vuelve a ejecutar el comando de la API con `-Configurar` para guardarlos de nuevo. Si aún debes crear el primer administrador, añade también `-CrearAdministrador`. La reconfiguración genera otra clave JWT y las sesiones abiertas deben volver a entrar. Si definiste `DB_USER`, `DB_PASSWORD` o `JWT_SECRET` en el entorno, sus valores tienen prioridad.
- **Angular indica “Correo o contraseña incorrectos”.** Usa las credenciales ADMIN del paso 3. Si ya existen usuarios, `-CrearAdministrador` conserva sus cuentas. Otro administrador puede restablecer la contraseña desde **Usuarios**.

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

**Usa V3 solo si partes de las cinco tablas del Avance 2 sin migrar. La instalación nueva ya incluye esos cambios.** La aplicación arranca con `ddl-auto=validate` y `spring.sql.init.mode=never`. Para migrar otra instalación del Avance 2:

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
