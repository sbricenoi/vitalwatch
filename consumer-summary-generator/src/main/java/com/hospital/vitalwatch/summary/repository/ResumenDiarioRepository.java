package com.hospital.vitalwatch.summary.repository;

import com.hospital.vitalwatch.summary.model.ResumenDiarioKafka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ResumenDiarioRepository extends JpaRepository<ResumenDiarioKafka, Long> {
    
    Optional<ResumenDiarioKafka> findByFecha(LocalDate fecha);
}
