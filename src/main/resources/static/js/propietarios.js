
document.addEventListener('DOMContentLoaded', () => {

    const API_URL = '/api/propietarios';

    // Referencias a elementos del DOM
    const tablaCuerpo = document.querySelector('.table-custom tbody');
    const formNuevoPropietario = document.getElementById('formNuevoPropietario');
    const inputBuscar = document.querySelector('.search-input');
    const modalElement = document.getElementById('modalNuevoPropietario');
    const bootstrapModal = new bootstrap.Modal(modalElement);

    // 1. Cargar propietarios al iniciar la página
    cargarPropietarios();

    // 2. Event Listener para el formulario del Modal (Crear Propietario)
    formNuevoPropietario.addEventListener('submit', async (e) => {
        e.preventDefault(); // Evita la recarga nativa de la página

        // Capturar y estructurar los datos del formulario
        const nuevoPropietario = {
            nombres: document.getElementById('nombres').value.trim(),
            apellidos: document.getElementById('apellidos').value.trim(),
            telefono: document.getElementById('telefono').value.trim(),
            correo: document.getElementById('correo').value.trim()
        };

        try {

            // Petición POST a la API REST
            const response = await fetch(API_URL, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(nuevoPropietario)
            });

            if (response.ok) {
                const data = await response.json();

                // Limpiar formulario y cerrar modal
                formNuevoPropietario.reset();
                bootstrapModal.hide();

                // Refrescar la tabla en tiempo real
                await cargarPropietarios();

            } else {
                const errorData = await response.json();
                alert(errorData.mensaje || 'Ocurrió un error al guardar el propietario.');
            }

        } catch (error) {
            console.error('Error de red:', error);
            alert('No se pudo conectar con el servidor.');
        }
    });

    // 3. Event Listener para búsqueda en tiempo real
    if (inputBuscar) {
        let timeoutBuscador = null;
        inputBuscar.addEventListener('input', (e) => {
            clearTimeout(timeoutBuscador);
            // Pequeño debounce para no saturar con peticiones en cada tecla
            timeoutBuscador = setTimeout(() => {
                cargarPropietarios(e.target.value.trim());
            }, 300);
        });
    }

    // --- FUNCIONES AUXILIARES ---

    // Obtener y listar propietarios desde la API
    async function cargarPropietarios(criterioBusqueda = '') {
        try {
            const url = criterioBusqueda
                ? `${API_URL}?buscar=${encodeURIComponent(criterioBusqueda)}`
                : API_URL;

            const response = await fetch(url);
            if (!response.ok) throw new Error('Error al obtener datos');

            const propietarios = await response.json();
            renderizarTabla(propietarios);

        } catch (error) {
            console.error('Error al cargar propietarios:', error);
            tablaCuerpo.innerHTML = `
                <tr>
                    <td colspan="5" class="text-center text-danger py-4">
                        <i class="bi bi-exclamation-triangle"></i> Error al cargar los registros.
                    </td>
                </tr>`;
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
});
