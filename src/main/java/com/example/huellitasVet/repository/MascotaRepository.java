package com.example.huellitasVet.repository;

import com.example.huellitasVet.model.Mascota;

import org.springframework.data.jpa.repository.*;

import java.util.List;

public interface MascotaRepository extends JpaRepository<Mascota, Long> {
    @Query(
            """
select m from Mascota m left join fetch m.propietario p
where (:propietarioId is null or p.id=:propietarioId) and (:especie='' or lower(m.especie)=lower(:especie))
and lower(m.nombre) like lower(concat('%',:q,'%')) order by m.nombre,m.id
""")
    List<Mascota> buscar(Long propietarioId, String especie, String q);
}
