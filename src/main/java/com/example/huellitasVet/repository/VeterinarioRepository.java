package com.example.huellitasVet.repository;

import com.example.huellitasVet.model.Veterinario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VeterinarioRepository extends JpaRepository<Veterinario, Long> {

    @Query("SELECT v FROM Veterinario v WHERE " +
            "LOWER(v.nombres) LIKE LOWER(CONCAT('%', :criterio, '%')) OR " +
            "LOWER(v.apellidos) LIKE LOWER(CONCAT('%', :criterio, '%')) OR " +
            "LOWER(v.especialidad) LIKE LOWER(CONCAT('%', :criterio, '%')) OR " +
            "LOWER(v.correo) LIKE LOWER(CONCAT('%', :criterio, '%'))")
    List<Veterinario> buscar(@Param("criterio") String criterio);
}