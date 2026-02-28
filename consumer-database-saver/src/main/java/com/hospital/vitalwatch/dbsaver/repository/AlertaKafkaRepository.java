package com.hospital.vitalwatch.dbsaver.repository;

import com.hospital.vitalwatch.dbsaver.model.AlertaKafka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AlertaKafkaRepository extends JpaRepository<AlertaKafka, Long> {
    
    boolean existsByAlertId(String alertId);
    
    @Query("SELECT COUNT(a) FROM AlertaKafka a WHERE a.detectedAt >= :from")
    Long countByDetectedAtAfter(LocalDateTime from);
    
    @Query("SELECT COUNT(a) FROM AlertaKafka a WHERE a.detectedAt >= :from AND a.severidad = :severidad")
    Long countBySeveridadAfter(String severidad, LocalDateTime from);
}
