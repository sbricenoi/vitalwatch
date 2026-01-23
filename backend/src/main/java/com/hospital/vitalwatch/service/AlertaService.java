package com.hospital.vitalwatch.service;

import com.hospital.vitalwatch.dto.AlertaDTO;
import com.hospital.vitalwatch.exception.ResourceNotFoundException;
import com.hospital.vitalwatch.model.Alerta;
import com.hospital.vitalwatch.model.Paciente;
import com.hospital.vitalwatch.repository.AlertaRepository;
import com.hospital.vitalwatch.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de Alertas
 * Contiene la lógica de negocio para la gestión de alertas médicas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertaService {

    private final AlertaRepository alertaRepository;
    private final PacienteRepository pacienteRepository;

    /**
     * Obtiene todas las alertas
     */
    @Transactional(readOnly = true)
    public List<AlertaDTO> obtenerTodas() {
        log.info("Obteniendo todas las alertas");
        return alertaRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una alerta por ID
     */
    @Transactional(readOnly = true)
    public AlertaDTO obtenerPorId(Long id) {
        log.info("Obteniendo alerta con ID: {}", id);
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Alerta no encontrada con ID: " + id));
        return convertirADTO(alerta);
    }

    /**
     * Obtiene alertas activas
     */
    @Transactional(readOnly = true)
    public List<AlertaDTO> obtenerActivas() {
        log.info("Obteniendo alertas activas");
        return alertaRepository.findByEstadoOrderByFechaCreacionDesc("ACTIVA").stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene alertas críticas activas
     */
    @Transactional(readOnly = true)
    public List<AlertaDTO> obtenerCriticasActivas() {
        log.info("Obteniendo alertas críticas activas");
        return alertaRepository.findAlertasCriticasActivas().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene alertas por paciente
     */
    @Transactional(readOnly = true)
    public List<AlertaDTO> obtenerPorPaciente(Long pacienteId) {
        log.info("Obteniendo alertas del paciente ID: {}", pacienteId);
        
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Paciente no encontrado con ID: " + pacienteId);
        }

        return alertaRepository.findByPacienteIdOrderByFechaCreacionDesc(pacienteId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene alertas activas de un paciente
     */
    @Transactional(readOnly = true)
    public List<AlertaDTO> obtenerActivasPorPaciente(Long pacienteId) {
        log.info("Obteniendo alertas activas del paciente ID: {}", pacienteId);
        
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Paciente no encontrado con ID: " + pacienteId);
        }

        return alertaRepository.findAlertasActivasPorPaciente(pacienteId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene alertas por severidad
     */
    @Transactional(readOnly = true)
    public List<AlertaDTO> obtenerPorSeveridad(String severidad) {
        log.info("Obteniendo alertas con severidad: {}", severidad);
        return alertaRepository.findBySeveridadOrderByFechaCreacionDesc(severidad).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene alertas recientes (últimas N)
     */
    @Transactional(readOnly = true)
    public List<AlertaDTO> obtenerRecientes(int limite) {
        log.info("Obteniendo últimas {} alertas", limite);
        return alertaRepository.findAlertasRecientes(limite).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Crea una alerta manual
     */
    @Transactional
    public AlertaDTO crear(AlertaDTO alertaDTO) {
        log.info("Creando alerta manual para paciente ID: {}", alertaDTO.getPacienteId());
        
        Paciente paciente = pacienteRepository.findById(alertaDTO.getPacienteId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Paciente no encontrado con ID: " + alertaDTO.getPacienteId()));

        Alerta alerta = new Alerta(
            paciente,
            alertaDTO.getTipo(),
            alertaDTO.getMensaje(),
            alertaDTO.getSeveridad()
        );

        Alerta alertaGuardada = alertaRepository.save(alerta);
        log.info("Alerta creada exitosamente con ID: {}", alertaGuardada.getId());

        return convertirADTO(alertaGuardada);
    }

    /**
     * Resuelve una alerta
     */
    @Transactional
    public AlertaDTO resolver(Long id, String resueltoPor, String notasResolucion) {
        log.info("Resolviendo alerta ID: {}", id);
        
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Alerta no encontrada con ID: " + id));

        alerta.resolver(resueltoPor, notasResolucion);
        
        Alerta alertaActualizada = alertaRepository.save(alerta);
        log.info("Alerta resuelta exitosamente: {}", id);

        return convertirADTO(alertaActualizada);
    }

    /**
     * Descarta una alerta
     */
    @Transactional
    public AlertaDTO descartar(Long id, String descartadoPor, String motivo) {
        log.info("Descartando alerta ID: {}", id);
        
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Alerta no encontrada con ID: " + id));

        alerta.descartar(descartadoPor, motivo);
        
        Alerta alertaActualizada = alertaRepository.save(alerta);
        log.info("Alerta descartada exitosamente: {}", id);

        return convertirADTO(alertaActualizada);
    }

    /**
     * Elimina una alerta
     */
    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando alerta con ID: {}", id);
        
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Alerta no encontrada con ID: " + id));

        alertaRepository.delete(alerta);
        log.info("Alerta eliminada exitosamente: {}", id);
    }

    /**
     * Obtiene estadísticas de alertas
     */
    @Transactional(readOnly = true)
    public Map<String, Long> obtenerEstadisticas() {
        log.info("Obteniendo estadísticas de alertas");
        
        Long totalActivas = alertaRepository.contarAlertasActivas();
        Long totalCriticas = alertaRepository.contarAlertasCriticasActivas();
        
        return Map.of(
            "alertasActivas", totalActivas,
            "alertasCriticas", totalCriticas
        );
    }

    /**
     * Obtiene estadísticas por severidad
     */
    @Transactional(readOnly = true)
    public List<Object[]> obtenerEstadisticasPorSeveridad() {
        log.info("Obteniendo estadísticas por severidad");
        return alertaRepository.obtenerEstadisticasPorSeveridad();
    }

    /**
     * Convierte entidad a DTO
     */
    private AlertaDTO convertirADTO(Alerta alerta) {
        return new AlertaDTO(
            alerta.getId(),
            alerta.getPaciente().getId(),
            alerta.getPaciente().getNombreCompleto(),
            alerta.getPaciente().getSala(),
            alerta.getPaciente().getCama(),
            alerta.getPaciente().getEstado(),
            alerta.getTipo(),
            alerta.getMensaje(),
            alerta.getSeveridad(),
            alerta.getEstado(),
            alerta.getFechaCreacion(),
            alerta.getFechaResolucion(),
            alerta.getResueltoPor(),
            alerta.getNotasResolucion()
        );
    }
}
