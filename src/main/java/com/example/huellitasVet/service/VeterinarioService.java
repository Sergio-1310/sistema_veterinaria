package com.example.huellitasVet.service;

import com.example.huellitasVet.model.Veterinario;
import com.example.huellitasVet.repository.VeterinarioRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor 
public class VeterinarioService {

    private final VeterinarioRepository veterinarioRepository;

    @Transactional(readOnly = true)
    public List<Veterinario> listarTodos() {
        return veterinarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Veterinario> buscar(String criterio) {
        if (criterio == null || criterio.trim().isEmpty()) {
            return listarTodos();
        }
        return veterinarioRepository.buscar(criterio.trim());
    }

    @Transactional(readOnly = true)
    public Veterinario buscarPorId(Long id) {
        return veterinarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado con id: " + id));
    }

    @Transactional
    public Veterinario guardar(Veterinario veterinario) {
        if (veterinario.getActivo() == null) {
            veterinario.setActivo(true);
        }
        return veterinarioRepository.save(veterinario);
    }
}