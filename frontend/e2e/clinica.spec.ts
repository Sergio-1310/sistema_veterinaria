import { test, expect, Page } from '@playwright/test';

test('recepción, agenda, atención, historial y permisos', async ({ page }) => {
  const email = process.env['E2E_EMAIL'],
    password = process.env['E2E_PASSWORD'];
  if (!email || !password)
    throw new Error('Define E2E_EMAIL y E2E_PASSWORD para la base aislada en el puerto 18083.');
  const sufijo = Date.now().toString();
  const correoVet = 'vet' + sufijo + '@example.invalid';
  const correoRecepcion = 'recepcion' + sufijo + '@example.invalid';
  const nombreMascota = 'Luna ' + sufijo;
  const errores: string[] = [];
  page.on('pageerror', (e) => errores.push(e.message));
  async function entrar(p: Page, correo: string) {
    await p.goto('/login');
    await p.getByLabel('Correo electrónico').fill(correo);
    await p.getByLabel('Contraseña', { exact: true }).fill(password!);
    await p.getByRole('button', { name: 'Ingresar', exact: true }).click();
    await expect(p.getByRole('heading', { name: 'Próximas citas' })).toBeVisible();
  }
  async function guardar() {
    await page.getByRole('button', { name: 'Guardar', exact: true }).click();
    await expect(page.getByText('Cambios guardados.')).toBeVisible();
  }
  async function usuario(nombre: string, correo: string, rol: string) {
    await page.goto('/usuarios/nuevo');
    await page.getByLabel('Nombre completo').fill(nombre);
    await page.getByLabel('Correo electrónico').fill(correo);
    await page.getByLabel('Rol', { exact: true }).selectOption(rol);
    await page.getByLabel('Contraseña nueva').fill(password!);
    await guardar();
  }
  await page.goto('/inicio');
  await expect(page).toHaveURL(/\/login$/);
  await entrar(page, email);
  await usuario('Recepción de prueba', correoRecepcion, 'RECEPCIONISTA');
  await usuario('Profesional de prueba', correoVet, 'VETERINARIO');
  await page.goto('/veterinarios/nuevo');
  await page.getByLabel('Nombres').fill('Ana');
  await page.getByLabel('Apellidos').fill(sufijo);
  await page.getByLabel('Correo electrónico').fill(correoVet);
  await guardar();
  await page.getByRole('button', { name: 'Cerrar sesión' }).click();
  await entrar(page, correoRecepcion);
  await page.goto('/usuarios');
  await expect(page.getByRole('heading', { name: 'Acceso denegado' })).toBeVisible();
  await page.goto('/propietarios/nuevo');
  await expect(page.getByRole('button', { name: 'Guardar', exact: true })).toBeDisabled();
  await page.getByLabel('Nombres').fill('Sol');
  await page.getByLabel('Apellidos').fill(sufijo);
  await page.getByLabel('Correo electrónico').fill('sol' + sufijo + '@example.invalid');
  await page.getByLabel('Teléfono').fill('999222333');
  await guardar();
  const propietarioUrl = page.url();
  await page.getByRole('link', { name: 'Editar', exact: true }).click();
  await page.getByLabel('Teléfono').fill('999222334');
  await guardar();
  await expect(page.getByText('999222334', { exact: true })).toBeVisible();
  await page.getByRole('link', { name: 'Registrar mascota' }).click();
  await page.getByLabel('Nombre', { exact: true }).fill(nombreMascota);
  await page.getByLabel('Especie').fill('Gato');
  await guardar();
  const mascotaUrl = page.url();
  await page.getByRole('link', { name: 'Editar', exact: true }).click();
  await page.getByLabel('Raza').fill('Mestiza');
  await guardar();
  await page.goto(propietarioUrl);
  await expect(page.getByRole('link', { name: nombreMascota, exact: true })).toBeVisible();
  await page.goto(mascotaUrl);
  await page.getByRole('link', { name: 'Programar cita' }).click();
  await page.getByLabel('Veterinario responsable').selectOption({ label: 'Ana ' + sufijo });
  const futuro = new Date();
  futuro.setDate(futuro.getDate() + 2);
  const fecha = [
    futuro.getFullYear(),
    String(futuro.getMonth() + 1).padStart(2, '0'),
    String(futuro.getDate()).padStart(2, '0'),
  ].join('-');
  await page.getByLabel('Fecha', { exact: true }).fill(fecha);
  await page.getByLabel('Hora').fill('10:00');
  await page.getByLabel('Motivo de consulta').fill('Control');
  await guardar();
  const primera = page.url();
  await page.getByRole('button', { name: 'Cancelar cita' }).click();
  await page.getByRole('dialog').getByRole('button', { name: 'Confirmar' }).click();
  await expect(page.getByText('Cita cancelada.')).toBeVisible();
  await page.goto(primera);
  await expect(page.getByText('Cancelada', { exact: true })).toBeVisible();
  await page.goto(mascotaUrl);
  await page.getByRole('link', { name: 'Programar cita' }).click();
  await page.getByLabel('Veterinario responsable').selectOption({ label: 'Ana ' + sufijo });
  await page.getByLabel('Fecha', { exact: true }).fill(fecha);
  await page.getByLabel('Hora').fill('10:00');
  await page.getByLabel('Motivo de consulta').fill('Control reprogramado');
  await guardar();
  const citaUrl = page.url();
  await page.getByRole('link', { name: 'Editar', exact: true }).click();
  await page.getByLabel('Hora').fill('11:00');
  await guardar();
  await page.getByRole('button', { name: 'Cerrar sesión' }).click();
  await entrar(page, correoVet);
  await page.goto(citaUrl);
  await page.getByRole('link', { name: 'Registrar atención' }).click();
  await page.getByLabel('Diagnóstico').fill('Control sin hallazgos');
  await page.getByLabel('Tratamiento').fill('Seguimiento');
  await page.getByRole('button', { name: 'Finalizar atención' }).click();
  await expect(page).toHaveURL(/\/historial\/\d+$/);
  await page.reload();
  await expect(page.getByText('Control sin hallazgos', { exact: true })).toBeVisible();
  await page.goto(citaUrl);
  await expect(page.getByText('Atendida', { exact: true })).toBeVisible();
  await page.goto('/historial');
  await page.getByRole('textbox', { name: 'Buscar' }).fill(nombreMascota);
  await page.getByRole('button', { name: 'Buscar', exact: true }).click();
  await expect(page.getByRole('link', { name: nombreMascota })).toBeVisible();
  await page.getByRole('button', { name: 'Cerrar sesión' }).click();
  await page.goto(mascotaUrl);
  await expect(page).toHaveURL(/\/login$/);
  expect(errores).toEqual([]);
});


test('la ficha anterior sigue disponible tras reiniciar Angular y Spring Boot', async ({ page }) => {
  const anterior = process.env['E2E_FICHA_ANTERIOR'];
  test.skip(!anterior, 'Requiere el ID de una ficha de la ejecución previa al reinicio.');
  await page.goto('/login');
  await page.getByLabel('Correo electrónico').fill(process.env['E2E_EMAIL']!);
  await page.getByLabel('Contraseña', { exact: true }).fill(process.env['E2E_PASSWORD']!);
  await page.getByRole('button', { name: 'Ingresar', exact: true }).click();
  await expect(page.getByRole('heading', { name: 'Próximas citas' })).toBeVisible();
  await page.goto('/historial/' + anterior);
  await expect(page.getByText('Control sin hallazgos', { exact: true })).toBeVisible();
  await page.getByRole('link', { name: 'Ver cita', exact: true }).click();
  await expect(page.getByText('Atendida', { exact: true })).toBeVisible();
});
