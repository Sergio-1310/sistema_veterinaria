package com.example.huellitasVet.service;

import com.example.huellitasVet.model.Propietario;
import com.example.huellitasVet.repository.PropietarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PropietarioService {

    private final PropietarioRepository propietarioRepository;

    @Transactional(readOnly = true)
    public List<Propietario> obtenerTodos() {
        return propietarioRepository.findAll();
    }

    @Transactional (readOnly = true)
    public Optional<Propietario> obtenerPorId(Long id) {
        return propietarioRepository.findById(id);
    }

    @Transactional (readOnly = true)
    public List<Propietario> buscar(String criterio) {
        if (criterio == null || criterio.trim().isEmpty()) {
            return obtenerTodos();
        }
        return propietarioRepository
            .findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrCorreoContainingIgnoreCase(criterio, criterio, criterio);
    }

    @Transactional 
    public Propietario guardar(Propietario propietario) {
        if (propietario.getId() == null && propietarioRepository.existsByCorreo(propietario.getCorreo())) {
            throw new IllegalArgumentException("Ya existe un propietario registrado con ese correo.");
        }
        return propietarioRepository.save(propietario);
    }

    @Transactional 
    public void eliminar(Long id) {
        propietarioRepository.deleteById(id);
    }
}