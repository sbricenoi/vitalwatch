package com.hospital.vitalwatch.service;

import com.hospital.vitalwatch.dto.SignosVitalesDTO;
import com.hospital.vitalwatch.exception.ResourceNotFoundException;
import com.hospital.vitalwatch.model.Paciente;
import com.hospital.vitalwatch.model.SignosVitales;
import com.hospital.vitalwatch.repository.PacienteRepository;
import com.hospital.vitalwatch.repository.SignosVitalesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de Signos Vitales
 * Contiene la lógica de negocio para el registro y consulta de signos vitales
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SignosVitalesService {

    private final SignosVitalesRepository signosVitalesRepository;
    private final PacienteRepository pacienteRepository;
    private final MonitoreoService monitoreoService;

    /**
     * Obtiene todos los signos vitales
     */
    @Transactional(readOnly = true)
    public List<SignosVitalesDTO> obtenerTodos() {
        log.info("Obteniendo todos los signos vitales");
        return signosVitalesRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene signos vitales por ID
     */
    @Transactional(readOnly = true)
    public SignosVitalesDTO obtenerPorId(Long id) {
        log.info("Obteniendo signos vitales con ID: {}", id);
        SignosVitales signosVitales = signosVitalesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Signos vitales no encontrados con ID: " + id));
        return convertirADTO(signosVitales);
    }

    /**
     * Obtiene historial de signos vitales de un paciente
     */
    @Transactional(readOnly = true)
    public List<SignosVitalesDTO> obtenerPorPaciente(Long pacienteId) {
        log.info("Obteniendo signos vitales del paciente ID: {}", pacienteId);
        
        // Verificar que el paciente existe
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Paciente no encontrado con ID: " + pacienteId);
        }

        return signosVitalesRepository.findByPacienteIdOrderByFechaRegistroDesc(pacienteId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el último registro de signos vitales de un paciente
     */
    @Transactional(readOnly = true)
    public SignosVitalesDTO obtenerUltimoRegistro(Long pacienteId) {
        log.info("Obteniendo último registro de signos vitales del paciente ID: {}", pacienteId);
        
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Paciente no encontrado con ID: " + pacienteId);
        }

        SignosVitales signosVitales = signosVitalesRepository
                .findUltimoRegistroPorPaciente(pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "No hay registros de signos vitales para el paciente ID: " + pacienteId));
        
        return convertirADTO(signosVitales);
    }

    /**
     * Obtiene últimos N registros de un paciente
     */
    @Transactional(readOnly = true)
    public List<SignosVitalesDTO> obtenerUltimosRegistros(Long pacienteId, int limite) {
        log.info("Obteniendo últimos {} registros del paciente ID: {}", limite, pacienteId);
        
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Paciente no encontrado con ID: " + pacienteId);
        }

        return signosVitalesRepository.findUltimosRegistrosPorPaciente(pacienteId, limite).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Registra nuevos signos vitales
     * IMPORTANTE: Genera alertas automáticamente si los valores están fuera de rango
     */
    @Transactional
    public SignosVitalesDTO registrar(SignosVitalesDTO signosVitalesDTO) {
        log.info("Registrando signos vitales para paciente ID: {}", signosVitalesDTO.getPacienteId());
        
        Paciente paciente = pacienteRepository.findById(signosVitalesDTO.getPacienteId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Paciente no encontrado con ID: " + signosVitalesDTO.getPacienteId()));

        SignosVitales signosVitales = convertirAEntidad(signosVitalesDTO, paciente);
        
        if (signosVitales.getFechaRegistro() == null) {
            signosVitales.setFechaRegistro(LocalDateTime.now());
        }

        SignosVitales signosGuardados = signosVitalesRepository.save(signosVitales);
        log.info("Signos vitales registrados exitosamente con ID: {}", signosGuardados.getId());

        // IMPORTANTE: Evaluar si se deben generar alertas automáticamente
        monitoreoService.evaluarYGenerarAlertas(signosGuardados);

        return convertirADTO(signosGuardados);
    }

    /**
     * Actualiza signos vitales existentes
     */
    @Transactional
    public SignosVitalesDTO actualizar(Long id, SignosVitalesDTO signosVitalesDTO) {
        log.info("Actualizando signos vitales con ID: {}", id);
        
        SignosVitales signosExistentes = signosVitalesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Signos vitales no encontrados con ID: " + id));

        // Actualizar campos
        signosExistentes.setFrecuenciaCardiaca(signosVitalesDTO.getFrecuenciaCardiaca());
        signosExistentes.setPresionSistolica(signosVitalesDTO.getPresionSistolica());
        signosExistentes.setPresionDiastolica(signosVitalesDTO.getPresionDiastolica());
        signosExistentes.setTemperatura(signosVitalesDTO.getTemperatura());
        signosExistentes.setSaturacionOxigeno(signosVitalesDTO.getSaturacionOxigeno());
        signosExistentes.setFrecuenciaRespiratoria(signosVitalesDTO.getFrecuenciaRespiratoria());
        signosExistentes.setEstadoConciencia(signosVitalesDTO.getEstadoConciencia());
        signosExistentes.setObservaciones(signosVitalesDTO.getObservaciones());

        SignosVitales signosActualizados = signosVitalesRepository.save(signosExistentes);
        log.info("Signos vitales actualizados exitosamente: {}", id);

        return convertirADTO(signosActualizados);
    }

    /**
     * Elimina signos vitales
     */
    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando signos vitales con ID: {}", id);
        
        SignosVitales signosVitales = signosVitalesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Signos vitales no encontrados con ID: " + id));

        signosVitalesRepository.delete(signosVitales);
        log.info("Signos vitales eliminados exitosamente: {}", id);
    }

    /**
     * Obtiene registros con valores críticos
     */
    @Transactional(readOnly = true)
    public List<SignosVitalesDTO> obtenerRegistrosCriticos(LocalDateTime desde) {
        log.info("Obteniendo registros críticos desde: {}", desde);
        
        List<SignosVitales> registrosCriticos = signosVitalesRepository
                .findRegistrosConFrecuenciaCardiacaCritica(desde);
        
        return registrosCriticos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Convierte entidad a DTO
     */
    private SignosVitalesDTO convertirADTO(SignosVitales signosVitales) {
        SignosVitalesDTO dto = new SignosVitalesDTO();
        dto.setId(signosVitales.getId());
        dto.setPacienteId(signosVitales.getPaciente().getId());
        dto.setPacienteNombre(signosVitales.getPaciente().getNombreCompleto());
        dto.setPacienteSala(signosVitales.getPaciente().getSala());
        dto.setPacienteCama(signosVitales.getPaciente().getCama());
        dto.setFrecuenciaCardiaca(signosVitales.getFrecuenciaCardiaca());
        dto.setPresionSistolica(signosVitales.getPresionSistolica());
        dto.setPresionDiastolica(signosVitales.getPresionDiastolica());
        dto.setTemperatura(signosVitales.getTemperatura());
        dto.setSaturacionOxigeno(signosVitales.getSaturacionOxigeno());
        dto.setFrecuenciaRespiratoria(signosVitales.getFrecuenciaRespiratoria());
        dto.setEstadoConciencia(signosVitales.getEstadoConciencia());
        dto.setObservaciones(signosVitales.getObservaciones());
        dto.setFechaRegistro(signosVitales.getFechaRegistro());
        dto.setRegistradoPor(signosVitales.getRegistradoPor());
        
        // Campos calculados
        dto.setTieneAlgunaAnormalidad(!signosVitales.frecuenciaCardiacaNormal() ||
                                     !signosVitales.presionSistolicaNormal() ||
                                     !signosVitales.presionDiastolicaNormal() ||
                                     !signosVitales.temperaturaNormal() ||
                                     !signosVitales.saturacionOxigenoNormal() ||
                                     !signosVitales.frecuenciaRespiratoriaNormal());
        dto.setEsCritico(signosVitales.tieneSignoCritico());
        
        return dto;
    }

    /**
     * Convierte DTO a entidad
     */
    private SignosVitales convertirAEntidad(SignosVitalesDTO dto, Paciente paciente) {
        return new SignosVitales(
            paciente,
            dto.getFrecuenciaCardiaca(),
            dto.getPresionSistolica(),
            dto.getPresionDiastolica(),
            dto.getTemperatura(),
            dto.getSaturacionOxigeno(),
            dto.getFrecuenciaRespiratoria(),
            dto.getEstadoConciencia(),
            dto.getObservaciones(),
            dto.getFechaRegistro(),
            dto.getRegistradoPor()
        );
    }
}
