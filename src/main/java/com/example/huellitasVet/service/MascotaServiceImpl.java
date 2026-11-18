package com.example.huellitasVet.service;

import static com.example.huellitasVet.restController.ErroresApi.*;

import com.example.huellitasVet.dto.Solicitudes.Paciente;
import com.example.huellitasVet.model.Mascota;
import com.example.huellitasVet.repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MascotaServiceImpl implements MascotaService {
    private final MascotaRepository mascotas;
    private final PropietarioRepository propietarios;
    private final CitaRepository citas;

    public List<Mascota> buscar(Long propietarioId, String especie, String q) {
        return mascotas.buscar(propietarioId, especie, q);
    }

    public Mascota buscarPorId(Long id) {
        return mascotas.findById(id).orElseThrow(() -> ausente("Paciente"));
    }

    public Mascota guardar(Long id, Paciente datos) {
        var mascota = id == null ? new Mascota() : buscarPorId(id);
        var propietario =
                propietarios
                        .findById(datos.propietarioId())
                        .orElseThrow(() -> ausente("Propietario"));
        mascota.setNombre(datos.nombre().trim());
        mascota.setEspecie(datos.especie().trim());
        mascota.setRaza(datos.raza());
        mascota.setSexo(datos.sexo());
        mascota.setFechaNacimiento(datos.fechaNacimiento());
        mascota.setPropietario(propietario);
        return mascotas.save(mascota);
    }

    public void eliminar(Long id) {
        if (!citas.buscar(id, null, null, null, "").isEmpty())
            throw conflicto("La mascota tiene citas asociadas.");
        mascotas.delete(buscarPorId(id));
    }
}
