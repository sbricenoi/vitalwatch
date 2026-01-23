package com.hospital.vitalwatch.controller;

import com.hospital.vitalwatch.dto.ApiResponse;
import com.hospital.vitalwatch.dto.PacienteDTO;
import com.hospital.vitalwatch.service.PacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de Pacientes
 * Expone endpoints para operaciones CRUD de pacientes
 */
@RestController
@RequestMapping("/api/v1/pacientes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pacientes", description = "API para gestión de pacientes hospitalarios")
public class PacienteController {

    private final PacienteService pacienteService;

    @Operation(summary = "Listar todos los pacientes")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PacienteDTO>>> obtenerTodos() {
        log.info("GET /api/v1/pacientes - Obteniendo todos los pacientes");
        List<PacienteDTO> pacientes = pacienteService.obtenerTodos();
        return ResponseEntity.ok(
            ApiResponse.success("Pacientes obtenidos exitosamente", pacientes)
        );
    }

    @Operation(summary = "Obtener paciente por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PacienteDTO>> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/v1/pacientes/{} - Obteniendo paciente", id);
        PacienteDTO paciente = pacienteService.obtenerPorId(id);
        return ResponseEntity.ok(
            ApiResponse.success("Paciente obtenido exitosamente", paciente)
        );
    }

    @Operation(summary = "Obtener pacientes por estado")
    @GetMapping("/estado/{estado}")
    public ResponseEntity<ApiResponse<List<PacienteDTO>>> obtenerPorEstado(
            @PathVariable String estado) {
        log.info("GET /api/v1/pacientes/estado/{} - Obteniendo pacientes por estado", estado);
        List<PacienteDTO> pacientes = pacienteService.obtenerPorEstado(estado);
        return ResponseEntity.ok(
            ApiResponse.success(
                "Pacientes con estado " + estado + " obtenidos exitosamente", 
                pacientes
            )
        );
    }

    @Operation(summary = "Obtener pacientes por sala")
    @GetMapping("/sala/{sala}")
    public ResponseEntity<ApiResponse<List<PacienteDTO>>> obtenerPorSala(
            @PathVariable String sala) {
        log.info("GET /api/v1/pacientes/sala/{} - Obteniendo pacientes por sala", sala);
        List<PacienteDTO> pacientes = pacienteService.obtenerPorSala(sala);
        return ResponseEntity.ok(
            ApiResponse.success(
                "Pacientes en sala " + sala + " obtenidos exitosamente", 
                pacientes
            )
        );
    }

    @Operation(summary = "Obtener pacientes críticos")
    @GetMapping("/criticos")
    public ResponseEntity<ApiResponse<List<PacienteDTO>>> obtenerPacientesCriticos() {
        log.info("GET /api/v1/pacientes/criticos - Obteniendo pacientes críticos");
        List<PacienteDTO> pacientes = pacienteService.obtenerPacientesCriticos();
        return ResponseEntity.ok(
            ApiResponse.success("Pacientes críticos obtenidos exitosamente", pacientes)
        );
    }

    @Operation(summary = "Buscar pacientes por nombre o apellido")
    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<PacienteDTO>>> buscarPorNombre(
            @RequestParam String texto) {
        log.info("GET /api/v1/pacientes/buscar?texto={} - Buscando pacientes", texto);
        List<PacienteDTO> pacientes = pacienteService.buscarPorNombre(texto);
        return ResponseEntity.ok(
            ApiResponse.success("Búsqueda completada exitosamente", pacientes)
        );
    }

    @Operation(summary = "Crear nuevo paciente")
    @PostMapping
    public ResponseEntity<ApiResponse<PacienteDTO>> crear(
            @Valid @RequestBody PacienteDTO pacienteDTO) {
        log.info("POST /api/v1/pacientes - Creando nuevo paciente");
        PacienteDTO pacienteCreado = pacienteService.crear(pacienteDTO);
        return new ResponseEntity<>(
            ApiResponse.created("Paciente creado exitosamente", pacienteCreado),
            HttpStatus.CREATED
        );
    }

    @Operation(summary = "Actualizar paciente existente")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PacienteDTO>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PacienteDTO pacienteDTO) {
        log.info("PUT /api/v1/pacientes/{} - Actualizando paciente", id);
        PacienteDTO pacienteActualizado = pacienteService.actualizar(id, pacienteDTO);
        return ResponseEntity.ok(
            ApiResponse.success("Paciente actualizado exitosamente", pacienteActualizado)
        );
    }

    @Operation(summary = "Eliminar paciente")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/v1/pacientes/{} - Eliminando paciente", id);
        pacienteService.eliminar(id);
        return ResponseEntity.ok(
            ApiResponse.success("Paciente eliminado exitosamente", null)
        );
    }
}
