package com.hospital.vitalwatch.repository;

import com.hospital.vitalwatch.model.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la entidad Alerta
 * Proporciona operaciones CRUD y consultas personalizadas
 */
@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    /**
     * Obtiene todas las alertas activas
     */
    List<Alerta> findByEstadoOrderByFechaCreacionDesc(String estado);

    /**
     * Obtiene alertas por severidad
     */
    List<Alerta> findBySeveridadOrderByFechaCreacionDesc(String severidad);

    /**
     * Obtiene alertas críticas activas
     */
    @Query("SELECT a FROM Alerta a WHERE a.severidad = 'CRÍTICA' AND a.estado = 'ACTIVA' " +
           "ORDER BY a.fechaCreacion DESC")
    List<Alerta> findAlertasCriticasActivas();

    /**
     * Obtiene todas las alertas de un paciente
     */
    List<Alerta> findByPacienteIdOrderByFechaCreacionDesc(Long pacienteId);

    /**
     * Obtiene alertas activas de un paciente
     */
    @Query("SELECT a FROM Alerta a WHERE a.paciente.id = :pacienteId AND a.estado = 'ACTIVA' " +
           "ORDER BY a.fechaCreacion DESC")
    List<Alerta> findAlertasActivasPorPaciente(@Param("pacienteId") Long pacienteId);

    /**
     * Obtiene alertas por tipo
     */
    List<Alerta> findByTipoOrderByFechaCreacionDesc(String tipo);

    /**
     * Obtiene alertas creadas después de una fecha
     */
    List<Alerta> findByFechaCreacionAfterOrderByFechaCreacionDesc(LocalDateTime fecha);

    /**
     * Cuenta alertas activas
     */
    @Query("SELECT COUNT(a) FROM Alerta a WHERE a.estado = 'ACTIVA'")
    Long contarAlertasActivas();

    /**
     * Cuenta alertas críticas activas
     */
    @Query("SELECT COUNT(a) FROM Alerta a WHERE a.severidad = 'CRÍTICA' AND a.estado = 'ACTIVA'")
    Long contarAlertasCriticasActivas();

    /**
     * Obtiene estadísticas de alertas por severidad
     */
    @Query("SELECT a.severidad, COUNT(a) FROM Alerta a WHERE a.estado = 'ACTIVA' GROUP BY a.severidad")
    List<Object[]> obtenerEstadisticasPorSeveridad();

    /**
     * Obtiene alertas por estado y severidad
     */
    @Query("SELECT a FROM Alerta a WHERE a.estado = :estado AND a.severidad = :severidad " +
           "ORDER BY a.fechaCreacion DESC")
    List<Alerta> findByEstadoAndSeveridad(
        @Param("estado") String estado,
        @Param("severidad") String severidad
    );

    /**
     * Obtiene alertas recientes (últimas N)
     */
    @Query("SELECT a FROM Alerta a ORDER BY a.fechaCreacion DESC LIMIT :limite")
    List<Alerta> findAlertasRecientes(@Param("limite") int limite);

    /**
     * Busca por quien resolvió
     */
    List<Alerta> findByResueltoPor(String resueltoPor);
}
