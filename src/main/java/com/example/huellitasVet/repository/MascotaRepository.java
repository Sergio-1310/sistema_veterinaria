package com.example.huellitasVet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.huellitasVet.model.Mascota;

public interface MascotaRepository extends JpaRepository<Mascota, Long> {
}