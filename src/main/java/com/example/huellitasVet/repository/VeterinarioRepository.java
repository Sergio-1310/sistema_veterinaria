package com.example.huellitasVet.repository;

import com.example.huellitasVet.model.Veterinario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface VeterinarioRepository extends JpaRepository<Veterinario, Long> {

    @Query("SELECT v FROM Veterinario v WHERE " +
            "LOWER(v.nombres) LIKE LOWER(CONCAT('%', :criterio, '%')) OR " +
            "LOWER(v.apellidos) LIKE LOWER(CONCAT('%', :criterio, '%')) OR " +
            "LOWER(v.especialidad) LIKE LOWER(CONCAT('%', :criterio, '%'))")
    List<Veterinario> buscar(@Param("criterio") String criterio);

    @Modifying
    @Query("UPDATE Veterinario v SET v.activo = false WHERE v.id = :id")
    int desactivar(@Param("id") Long id);
}