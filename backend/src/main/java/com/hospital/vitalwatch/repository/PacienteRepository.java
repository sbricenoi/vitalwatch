package com.hospital.vitalwatch.repository;

import com.hospital.vitalwatch.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Paciente
 * Proporciona operaciones CRUD y consultas personalizadas
 */
@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    /**
     * Busca un paciente por RUT
     */
    Optional<Paciente> findByRut(String rut);

    /**
     * Busca pacientes por estado
     */
    List<Paciente> findByEstado(String estado);

    /**
     * Busca pacientes por sala
     */
    List<Paciente> findBySala(String sala);

    /**
     * Busca pacientes por sala y cama
     */
    Optional<Paciente> findBySalaAndCama(String sala, String cama);

    /**
     * Busca pacientes críticos
     */
    @Query("SELECT p FROM Paciente p WHERE p.estado = 'CRÍTICO'")
    List<Paciente> findPacientesCriticos();

    /**
     * Busca pacientes por nombre o apellido (búsqueda parcial)
     */
    @Query("SELECT p FROM Paciente p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) " +
           "OR LOWER(p.apellido) LIKE LOWER(CONCAT('%', :texto, '%'))")
    List<Paciente> buscarPorNombreOApellido(@Param("texto") String texto);

    /**
     * Cuenta pacientes por estado
     */
    @Query("SELECT COUNT(p) FROM Paciente p WHERE p.estado = :estado")
    Long contarPorEstado(@Param("estado") String estado);

    /**
     * Obtiene estadísticas de pacientes por estado
     */
    @Query("SELECT p.estado, COUNT(p) FROM Paciente p GROUP BY p.estado")
    List<Object[]> obtenerEstadisticasPorEstado();

    /**
     * Verifica si existe un paciente con el RUT dado
     */
    boolean existsByRut(String rut);

    /**
     * Verifica si existe un paciente en esa sala y cama
     */
    boolean existsBySalaAndCama(String sala, String cama);
}
