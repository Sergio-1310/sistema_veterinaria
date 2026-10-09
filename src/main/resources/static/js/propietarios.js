
document.addEventListener('DOMContentLoaded', () => {

    function initPropietarios() {
        const API_URL = '/api/propietarios';
        const tablaCuerpo = document.getElementById('tablaPropietariosCuerpo');
        const formNuevoPropietario = document.getElementById('formNuevoPropietario');
        const inputBuscar = document.querySelector('.search-input');
        const modalElement = document.getElementById('modalNuevoPropietario');

        if (!tablaCuerpo) return; // Si no estamos en la vista de propietarios, ignora

        const bootstrapModal = modalElement ? new bootstrap.Modal(modalElement) : null;

        // 1. Cargar tabla desde REST API
        async function cargarPropietarios(criterio = '') {
            try {
                const url = criterio ? `${API_URL}?buscar=${encodeURIComponent(criterio)}` : API_URL;
                const res = await fetch(url);
                if (!res.ok) throw new Error();
                const lista = await res.json();

                if (lista.length === 0) {
                    tablaCuerpo.innerHTML = `<tr><td colspan="5" class="text-center text-muted py-4">No hay registros.</td></tr>`;
                    return;
                }

                tablaCuerpo.innerHTML = lista.map(p => `
                <tr>
                    <td>
                        <div class="d-flex align-items-center">
                            <span class="owner-avatar">${(p.nombres?.[0] || '') + (p.apellidos?.[0] || '')}</span>
                            <span class="fw-medium text-dark">${p.nombres} ${p.apellidos}</span>
                        </div>
                    </td>
                    <td>${p.telefono}</td>
                    <td>${p.correo}</td>
                    <td><span class="pets-count"><i class="bi bi-paw text-muted"></i> ${p.mascotas?.length || 0} mascotas</span></td>
                    <td>
                        <a href="#" class="action-link"><i class="bi bi-eye"></i> Ver</a>
                        <a href="#" class="action-link"><i class="bi bi-pencil"></i> Editar</a>
                    </td>
                </tr>
            `).join('');
            } catch (e) {
                tablaCuerpo.innerHTML = `<tr><td colspan="5" class="text-center text-danger py-4">Error al cargar.</td></tr>`;
            }
        }

        cargarPropietarios();

        // 2. Guardar nuevo propietario
        if (formNuevoPropietario) {
            formNuevoPropietario.addEventListener('submit', async (e) => {
                e.preventDefault();
                const datos = {
                    nombres: document.getElementById('nombres').value.trim(),
                    apellidos: document.getElementById('apellidos').value.trim(),
                    telefono: document.getElementById('telefono').value.trim(),
                    correo: document.getElementById('correo').value.trim()
                };

                const res = await fetch(API_URL, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(datos)
                });

                if (res.ok) {
                    formNuevoPropietario.reset();
                    if (bootstrapModal) bootstrapModal.hide();
                    await cargarPropietarios();
                } else {
                    const err = await res.json();
                    alert(err.mensaje || 'Error al guardar.');
                }
            });
        }

        // 3. Buscador con Debounce
        if (inputBuscar) {
            let timer = null;
            inputBuscar.addEventListener('input', (e) => {
                clearTimeout(timer);
                timer = setTimeout(() => cargarPropietarios(e.target.value.trim()), 300);
            });
        }
    }

    // Dibujar las filas de la tabla en el DOM
    function renderizarTabla(lista) {
        if (lista.length === 0) {
            tablaCuerpo.innerHTML = `
                <tr>
                    <td colspan="5" class="text-center text-muted py-4">
                        No se encontraron propietarios registrados.
                    </td>
                </tr>`;
            return;
        }

        tablaCuerpo.innerHTML = lista.map(p => {
            const iniciales = obtenerIniciales(p.nombres, p.apellidos);
            const cantidadMascotas = p.mascotas ? p.mascotas.length : 0; // Ajustar según DTO/entidad
            const textoMascotas = cantidadMascotas === 1 ? '1 mascota' : `${cantidadMascotas} mascotas`;

            return `
                <tr>
                    <td>
                        <div class="d-flex align-items-center">
                            <span class="owner-avatar">${iniciales}</span>
                            <span class="fw-medium text-dark">${p.nombres} ${p.apellidos}</span>
                        </div>
                    </td>
                    <td>${p.telefono}</td>
                    <td>${p.correo}</td>
                    <td>
                        <span class="pets-count">
                            <i class="bi bi-paw text-muted"></i> ${textoMascotas}
                        </span>
                    </td>
                    <td>
                        <a href="#" class="action-link" onclick="verPropietario(${p.id}); return false;">
                            <i class="bi bi-eye"></i> Ver
                        </a>
                        <a href="#" class="action-link" onclick="editarPropietario(${p.id}); return false;">
                            <i class="bi bi-pencil"></i> Editar
                        </a>
                    </td>
                </tr>`;
        }).join('');
    }

    // Generar iniciales (Ej: "María" "García" -> "MG")
    function obtenerIniciales(nombres, apellidos) {
        const n = nombres ? nombres.trim().charAt(0).toUpperCase() : '';
        const a = apellidos ? apellidos.trim().charAt(0).toUpperCase() : '';
        return `${n}${a}` || 'P';
    }

    // Inicializar al cargar el DOM o cuando HTMX inyecte el fragmento
    document.addEventListener('DOMContentLoaded', initPropietarios);
    initPropietarios();
});
