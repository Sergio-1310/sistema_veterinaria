package com.example.huellitasVet.repository;

import com.example.huellitasVet.model.Cita;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;

import java.time.*;
import java.util.*;

public interface CitaRepository extends JpaRepository<Cita, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cita c where c.id=:id")
    Optional<Cita> paraEditar(Long id);

    boolean existsByVeterinarioIdAndFechaProgramadaAndHoraProgramadaAndEstadoAndIdNot(
            Long veterinario, LocalDate fecha, LocalTime hora, String estado, Long id);

    long countByEstado(String estado);

    long countByEstadoAndFechaProgramada(String estado, LocalDate fecha);

    @Query(
            "select c from Cita c where c.estado='PROGRAMADA' and (c.fechaProgramada>:hoy or"
                + " (c.fechaProgramada=:hoy and c.horaProgramada>=:hora)) order by"
                + " c.fechaProgramada,c.horaProgramada,c.id")
    List<Cita> proximas(
            LocalDate hoy, LocalTime hora, org.springframework.data.domain.Pageable limite);

    @Query(
            """
select c from Cita c left join fetch c.mascota m left join fetch c.veterinario v
where (:mascotaId is null or m.id=:mascotaId) and (:veterinarioId is null or v.id=:veterinarioId)
and (:fecha is null or c.fechaProgramada=:fecha) and (:estado is null or c.estado=:estado)
and (:q='' or lower(concat(coalesce(m.nombre,''),' ',coalesce(v.nombres,''),' ',coalesce(v.apellidos,''),' ',coalesce(c.motivo,''))) like lower(concat('%',:q,'%')))
order by c.fechaProgramada desc,c.horaProgramada,c.id
""")
    List<Cita> buscar(Long mascotaId, Long veterinarioId, LocalDate fecha, String estado, String q);
}
