package com.hospital.vitalwatch.service;

import com.hospital.vitalwatch.dto.PacienteDTO;
import com.hospital.vitalwatch.exception.ResourceNotFoundException;
import com.hospital.vitalwatch.exception.ValidationException;
import com.hospital.vitalwatch.model.Paciente;
import com.hospital.vitalwatch.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de Pacientes
 * Contiene la lógica de negocio para operaciones CRUD de pacientes
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    /**
     * Obtiene todos los pacientes
     */
    @Transactional(readOnly = true)
    public List<PacienteDTO> obtenerTodos() {
        log.info("Obteniendo todos los pacientes");
        return pacienteRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un paciente por ID
     */
    @Transactional(readOnly = true)
    public PacienteDTO obtenerPorId(Long id) {
        log.info("Obteniendo paciente con ID: {}", id);
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Paciente no encontrado con ID: " + id));
        return convertirADTO(paciente);
    }

    /**
     * Obtiene un paciente por RUT
     */
    @Transactional(readOnly = true)
    public PacienteDTO obtenerPorRut(String rut) {
        log.info("Obteniendo paciente con RUT: {}", rut);
        Paciente paciente = pacienteRepository.findByRut(rut)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Paciente no encontrado con RUT: " + rut));
        return convertirADTO(paciente);
    }

    /**
     * Obtiene pacientes por estado
     */
    @Transactional(readOnly = true)
    public List<PacienteDTO> obtenerPorEstado(String estado) {
        log.info("Obteniendo pacientes con estado: {}", estado);
        validarEstado(estado);
        return pacienteRepository.findByEstado(estado).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene pacientes por sala
     */
    @Transactional(readOnly = true)
    public List<PacienteDTO> obtenerPorSala(String sala) {
        log.info("Obteniendo pacientes en sala: {}", sala);
        return pacienteRepository.findBySala(sala).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene pacientes críticos
     */
    @Transactional(readOnly = true)
    public List<PacienteDTO> obtenerPacientesCriticos() {
        log.info("Obteniendo pacientes críticos");
        return pacienteRepository.findPacientesCriticos().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca pacientes por nombre o apellido
     */
    @Transactional(readOnly = true)
    public List<PacienteDTO> buscarPorNombre(String texto) {
        log.info("Buscando pacientes con texto: {}", texto);
        return pacienteRepository.buscarPorNombreOApellido(texto).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Crea un nuevo paciente
     */
    @Transactional
    public PacienteDTO crear(PacienteDTO pacienteDTO) {
        log.info("Creando nuevo paciente: {} {}", pacienteDTO.getNombre(), pacienteDTO.getApellido());
        
        // Validar que no exista un paciente con el mismo RUT
        if (pacienteRepository.existsByRut(pacienteDTO.getRut())) {
            throw new ValidationException("Ya existe un paciente con el RUT: " + pacienteDTO.getRut());
        }

        // Validar que no exista un paciente en esa sala/cama
        if (pacienteRepository.existsBySalaAndCama(pacienteDTO.getSala(), pacienteDTO.getCama())) {
            throw new ValidationException("Ya existe un paciente en la sala " + 
                pacienteDTO.getSala() + " cama " + pacienteDTO.getCama());
        }

        validarEstado(pacienteDTO.getEstado());
        validarGenero(pacienteDTO.getGenero());

        Paciente paciente = convertirAEntidad(pacienteDTO);
        if (paciente.getFechaIngreso() == null) {
            paciente.setFechaIngreso(LocalDateTime.now());
        }
        
        Paciente pacienteGuardado = pacienteRepository.save(paciente);
        log.info("Paciente creado exitosamente con ID: {}", pacienteGuardado.getId());
        
        return convertirADTO(pacienteGuardado);
    }

    /**
     * Actualiza un paciente existente
     */
    @Transactional
    public PacienteDTO actualizar(Long id, PacienteDTO pacienteDTO) {
        log.info("Actualizando paciente con ID: {}", id);
        
        Paciente pacienteExistente = pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Paciente no encontrado con ID: " + id));

        // Validar RUT si cambió
        if (!pacienteExistente.getRut().equals(pacienteDTO.getRut())) {
            if (pacienteRepository.existsByRut(pacienteDTO.getRut())) {
                throw new ValidationException("Ya existe un paciente con el RUT: " + pacienteDTO.getRut());
            }
        }

        // Validar sala/cama si cambió
        if (!pacienteExistente.getSala().equals(pacienteDTO.getSala()) || 
            !pacienteExistente.getCama().equals(pacienteDTO.getCama())) {
            if (pacienteRepository.existsBySalaAndCama(pacienteDTO.getSala(), pacienteDTO.getCama())) {
                throw new ValidationException("Ya existe un paciente en la sala " + 
                    pacienteDTO.getSala() + " cama " + pacienteDTO.getCama());
            }
        }

        validarEstado(pacienteDTO.getEstado());
        validarGenero(pacienteDTO.getGenero());

        // Actualizar campos
        pacienteExistente.setNombre(pacienteDTO.getNombre());
        pacienteExistente.setApellido(pacienteDTO.getApellido());
        pacienteExistente.setRut(pacienteDTO.getRut());
        pacienteExistente.setFechaNacimiento(pacienteDTO.getFechaNacimiento());
        pacienteExistente.setEdad(pacienteDTO.getEdad());
        pacienteExistente.setGenero(pacienteDTO.getGenero());
        pacienteExistente.setSala(pacienteDTO.getSala());
        pacienteExistente.setCama(pacienteDTO.getCama());
        pacienteExistente.setEstado(pacienteDTO.getEstado());
        pacienteExistente.setDiagnostico(pacienteDTO.getDiagnostico());
        
        if (pacienteDTO.getFechaAlta() != null) {
            pacienteExistente.setFechaAlta(pacienteDTO.getFechaAlta());
        }

        Paciente pacienteActualizado = pacienteRepository.save(pacienteExistente);
        log.info("Paciente actualizado exitosamente: {}", id);
        
        return convertirADTO(pacienteActualizado);
    }

    /**
     * Elimina un paciente (soft delete)
     */
    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando paciente con ID: {}", id);
        
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Paciente no encontrado con ID: " + id));

        pacienteRepository.delete(paciente);
        log.info("Paciente eliminado exitosamente: {}", id);
    }

    /**
     * Obtiene estadísticas de pacientes por estado
     */
    @Transactional(readOnly = true)
    public List<Object[]> obtenerEstadisticas() {
        log.info("Obteniendo estadísticas de pacientes");
        return pacienteRepository.obtenerEstadisticasPorEstado();
    }

    /**
     * Convierte entidad a DTO
     */
    private PacienteDTO convertirADTO(Paciente paciente) {
        return new PacienteDTO(
            paciente.getId(),
            paciente.getNombre(),
            paciente.getApellido(),
            paciente.getRut(),
            paciente.getFechaNacimiento(),
            paciente.getEdad(),
            paciente.getGenero(),
            paciente.getSala(),
            paciente.getCama(),
            paciente.getEstado(),
            paciente.getDiagnostico(),
            paciente.getFechaIngreso(),
            paciente.getFechaAlta()
        );
    }

    /**
     * Convierte DTO a entidad
     */
    private Paciente convertirAEntidad(PacienteDTO dto) {
        return new Paciente(
            dto.getNombre(),
            dto.getApellido(),
            dto.getRut(),
            dto.getFechaNacimiento(),
            dto.getEdad(),
            dto.getGenero(),
            dto.getSala(),
            dto.getCama(),
            dto.getEstado(),
            dto.getDiagnostico(),
            dto.getFechaIngreso()
        );
    }

    /**
     * Valida que el estado sea válido
     */
    private void validarEstado(String estado) {
        if (!estado.matches("ESTABLE|MODERADO|CRÍTICO|RECUPERACIÓN")) {
            throw new ValidationException("Estado inválido. Debe ser: ESTABLE, MODERADO, CRÍTICO o RECUPERACIÓN");
        }
    }

    /**
     * Valida que el género sea válido
     */
    private void validarGenero(String genero) {
        if (!genero.matches("[MFO]")) {
            throw new ValidationException("Género inválido. Debe ser: M, F u O");
        }
    }
}
