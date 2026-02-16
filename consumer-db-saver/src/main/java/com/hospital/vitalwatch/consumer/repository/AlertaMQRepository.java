package com.hospital.vitalwatch.consumer.repository;

import com.hospital.vitalwatch.consumer.entity.AlertaMQ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para alertas en Oracle
 */
@Repository
public interface AlertaMQRepository extends JpaRepository<AlertaMQ, Long> {

    /**
     * Buscar alertas por paciente
     */
    List<AlertaMQ> findByPacienteIdOrderByDetectedAtDesc(Long pacienteId);

    /**
     * Buscar alertas por severidad
     */
    List<AlertaMQ> findBySeverityOrderByDetectedAtDesc(String severity);

    /**
     * Contar alertas por rango de fechas
     */
    @Query("SELECT COUNT(a) FROM AlertaMQ a WHERE a.detectedAt BETWEEN ?1 AND ?2")
    Long countByDateRange(LocalDateTime start, LocalDateTime end);

    /**
     * Buscar alertas recientes
     */
    @Query("SELECT a FROM AlertaMQ a WHERE a.detectedAt >= ?1 ORDER BY a.detectedAt DESC")
    List<AlertaMQ> findRecentAlerts(LocalDateTime since);
}
