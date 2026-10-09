package com.example.huellitasVet.repository;

import com.example.huellitasVet.model.Propietario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface PropietarioRepository extends JpaRepository<Propietario, Long> {
    
    // Para el buscador en tiempo real
    List<Propietario> findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrCorreoContainingIgnoreCase(
            String nombres, String apellidos, String correo);

    boolean existsByCorreo(String correo);
}