document.addEventListener('DOMContentLoaded', () => {

    const API_URL = '/api/propietarios';

    const tablaCuerpo = document.querySelector('.table-custom tbody') || document.getElementById('tablaPropietariosCuerpo');
    const formNuevoPropietario = document.getElementById('formNuevoPropietario');
    const inputBuscar = document.querySelector('.search-input');
    const modalElement = document.getElementById('modalNuevoPropietario');
    const modalTitle = document.getElementById('modalNuevoPropietarioLabel');
    const inputId = document.getElementById('propietarioId');
    const bootstrapModal = new bootstrap.Modal(modalElement);

    cargarPropietarios();

    // 1. Guardar cambios (Crea con POST o actualiza con PUT)
    formNuevoPropietario.addEventListener('submit', async (e) => {
        e.preventDefault();

        const id = inputId.value;
        const propietarioData = {
            nombres: document.getElementById('nombres').value.trim(),
            apellidos: document.getElementById('apellidos').value.trim(),
            telefono: document.getElementById('telefono').value.trim(),
            correo: document.getElementById('correo').value.trim()
        };

        const esEdicion = Boolean(id);
        const url = esEdicion ? `${API_URL}/${id}` : API_URL;
        const metodo = esEdicion ? 'PUT' : 'POST';

        try {
            const response = await fetch(url, {
                method: metodo,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(propietarioData)
            });

            if (response.ok) {
                formNuevoPropietario.reset();
                inputId.value = '';
                bootstrapModal.hide();
                await cargarPropietarios();
            } else {
                const errorData = await response.json().catch(() => ({}));
                alert(errorData.mensaje || 'Ocurrió un error al guardar los cambios.');
            }
        } catch (error) {
            console.error('Error de red:', error);
            alert('No se pudo conectar con el servidor.');
        }
    });

    // 2. Buscador en tiempo real con debounce
    if (inputBuscar) {
        let timeoutBuscador = null;
        inputBuscar.addEventListener('input', (e) => {
            clearTimeout(timeoutBuscador);
            timeoutBuscador = setTimeout(() => {
                cargarPropietarios(e.target.value.trim());
            }, 300);
        });
    }

    // 3. Petición GET para listar
    async function cargarPropietarios(criterioBusqueda = '') {
        try {
            const url = criterioBusqueda
                ? `${API_URL}?buscar=${encodeURIComponent(criterioBusqueda)}`
                : API_URL;

            const response = await fetch(url);
            if (!response.ok) throw new Error('Error al consultar');

            const propietarios = await response.json();
            renderizarTabla(propietarios);
        } catch (error) {
            console.error('Error:', error);
            if (tablaCuerpo) {
                tablaCuerpo.innerHTML = `
                    <tr>
                        <td colspan="5" class="text-center text-danger py-4">
                            <i class="bi bi-exclamation-triangle"></i> Error al cargar los registros.
                        </td>
                    </tr>`;
            }
        }
    }

    // 4. Renderizar filas y conectar el botón Editar
    function renderizarTabla(lista) {
        if (!tablaCuerpo) return;

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
            const cantidadMascotas = p.mascotas ? p.mascotas.length : 0;
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
                        <a href="#" class="action-link" onclick="prepararEdicion(${p.id}, '${p.nombres}', '${p.apellidos}', '${p.telefono}', '${p.correo}'); return false;">
                            <i class="bi bi-pencil"></i> Editar
                        </a>
                    </td>
                </tr>`;
        }).join('');
    }

    function obtenerIniciales(nombres, apellidos) {
        const n = nombres ? nombres.trim().charAt(0).toUpperCase() : '';
        const a = apellidos ? apellidos.trim().charAt(0).toUpperCase() : '';
        return `${n}${a}` || 'P';
    }

    // 5. Funciones globales para controlar el modal
    window.prepararCreacion = function () {
        formNuevoPropietario.reset();
        inputId.value = '';
        if (modalTitle) modalTitle.textContent = 'Nuevo propietario';
    };

    window.prepararEdicion = function (id, nombres, apellidos, telefono, correo) {
        inputId.value = id;
        document.getElementById('nombres').value = nombres;
        document.getElementById('apellidos').value = apellidos;
        document.getElementById('telefono').value = telefono;
        document.getElementById('correo').value = correo;

        if (modalTitle) modalTitle.textContent = 'Editar propietario';
        bootstrapModal.show();
    };
});