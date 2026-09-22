package com.example.huellitasVet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.huellitasVet.model.Cita;

public interface CitaRepository extends JpaRepository<Cita, Long> {
}