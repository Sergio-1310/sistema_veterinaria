package com.example.huellitasVet.repository;

import com.example.huellitasVet.model.Atencion;

import org.springframework.data.jpa.repository.*;

import java.util.List;

public interface AtencionRepository extends JpaRepository<Atencion, Long> {
    @Query(
            """
select a from Atencion a join fetch a.cita c join fetch c.mascota m join fetch c.veterinario
where (:mascotaId is null or m.id=:mascotaId) and lower(m.nombre) like lower(concat('%',:q,'%'))
order by a.fecha desc,a.id desc
""")
    List<Atencion> buscar(Long mascotaId, String q);
}
