package com.example.huellitasVet.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.huellitasVet.model.Mascota;
import com.example.huellitasVet.repository.MascotaRepository;

@Service
public class MascotaServiceImpl implements MascotaService {

    private final MascotaRepository mascotaRepository;

    public MascotaServiceImpl(MascotaRepository mascotaRepository) {
        this.mascotaRepository = mascotaRepository;
    }

    @Override
    public List<Mascota> listar() {
        return mascotaRepository.findAll();
    }

    @Override
    public Mascota buscarPorId(Long id) {
        return mascotaRepository.findById(id).orElse(null);
    }

    @Override
    public Mascota registrar(Mascota mascota) {
        return mascotaRepository.save(mascota);
    }

    @Override
    public Mascota actualizar(Long id, Mascota mascota) {

        Mascota existente = buscarPorId(id);

        if (existente == null) {
            return null;
        }

        existente.setNombre(mascota.getNombre());
        existente.setEspecie(mascota.getEspecie());
        existente.setRaza(mascota.getRaza());
        existente.setEdad(mascota.getEdad());
        existente.setNombrePropietario(mascota.getNombrePropietario());

        return mascotaRepository.save(existente);
    }

    @Override
    public boolean eliminar(Long id) {

        if (!mascotaRepository.existsById(id)) {
            return false;
        }

        mascotaRepository.deleteById(id);
        return true;
    }
}
