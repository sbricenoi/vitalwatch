package com.hospital.vitalwatch.repository;

import com.hospital.vitalwatch.model.SignosVitales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad SignosVitales
 * Proporciona operaciones CRUD y consultas personalizadas
 */
@Repository
public interface SignosVitalesRepository extends JpaRepository<SignosVitales, Long> {

    /**
     * Obtiene todos los signos vitales de un paciente
     * Ordenados por fecha de registro descendente (más reciente primero)
     */
    List<SignosVitales> findByPacienteIdOrderByFechaRegistroDesc(Long pacienteId);

    /**
     * Obtiene el último registro de signos vitales de un paciente
     */
    @Query("SELECT sv FROM SignosVitales sv WHERE sv.paciente.id = :pacienteId " +
           "ORDER BY sv.fechaRegistro DESC LIMIT 1")
    Optional<SignosVitales> findUltimoRegistroPorPaciente(@Param("pacienteId") Long pacienteId);

    /**
     * Obtiene signos vitales de un paciente en un rango de fechas
     */
    @Query("SELECT sv FROM SignosVitales sv WHERE sv.paciente.id = :pacienteId " +
           "AND sv.fechaRegistro BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY sv.fechaRegistro DESC")
    List<SignosVitales> findByPacienteIdAndFechaRegistroBetween(
        @Param("pacienteId") Long pacienteId,
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin
    );

    /**
     * Obtiene registros con frecuencia cardíaca crítica
     */
    @Query("SELECT sv FROM SignosVitales sv WHERE " +
           "(sv.frecuenciaCardiaca > 120 OR sv.frecuenciaCardiaca < 50) " +
           "AND sv.fechaRegistro >= :desde")
    List<SignosVitales> findRegistrosConFrecuenciaCardiacaCritica(@Param("desde") LocalDateTime desde);

    /**
     * Obtiene registros con saturación de oxígeno crítica
     */
    @Query("SELECT sv FROM SignosVitales sv WHERE sv.saturacionOxigeno < 90 " +
           "AND sv.fechaRegistro >= :desde")
    List<SignosVitales> findRegistrosConSaturacionCritica(@Param("desde") LocalDateTime desde);

    /**
     * Cuenta registros de un paciente
     */
    Long countByPacienteId(Long pacienteId);

    /**
     * Obtiene últimos N registros de un paciente
     */
    @Query("SELECT sv FROM SignosVitales sv WHERE sv.paciente.id = :pacienteId " +
           "ORDER BY sv.fechaRegistro DESC LIMIT :limite")
    List<SignosVitales> findUltimosRegistrosPorPaciente(
        @Param("pacienteId") Long pacienteId,
        @Param("limite") int limite
    );

    /**
     * Busca por registrador
     */
    List<SignosVitales> findByRegistradoPor(String registradoPor);
}
