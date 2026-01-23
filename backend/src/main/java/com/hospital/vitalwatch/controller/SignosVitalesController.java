package com.hospital.vitalwatch.controller;

import com.hospital.vitalwatch.dto.ApiResponse;
import com.hospital.vitalwatch.dto.SignosVitalesDTO;
import com.hospital.vitalwatch.service.SignosVitalesService;
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
 * Controlador REST para gestión de Signos Vitales
 * Expone endpoints para registro y consulta de signos vitales
 */
@RestController
@RequestMapping("/api/v1/signos-vitales")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Signos Vitales", description = "API para registro y consulta de signos vitales")
public class SignosVitalesController {

    private final SignosVitalesService signosVitalesService;

    @Operation(summary = "Listar todos los signos vitales")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SignosVitalesDTO>>> obtenerTodos() {
        log.info("GET /api/v1/signos-vitales - Obteniendo todos los signos vitales");
        List<SignosVitalesDTO> signosVitales = signosVitalesService.obtenerTodos();
        return ResponseEntity.ok(
            ApiResponse.success("Signos vitales obtenidos exitosamente", signosVitales)
        );
    }

    @Operation(summary = "Obtener signos vitales por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SignosVitalesDTO>> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/v1/signos-vitales/{} - Obteniendo signos vitales", id);
        SignosVitalesDTO signosVitales = signosVitalesService.obtenerPorId(id);
        return ResponseEntity.ok(
            ApiResponse.success("Signos vitales obtenidos exitosamente", signosVitales)
        );
    }

    @Operation(summary = "Obtener historial de signos vitales de un paciente")
    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<ApiResponse<List<SignosVitalesDTO>>> obtenerPorPaciente(
            @PathVariable Long pacienteId) {
        log.info("GET /api/v1/signos-vitales/paciente/{} - Obteniendo historial", pacienteId);
        List<SignosVitalesDTO> signosVitales = signosVitalesService.obtenerPorPaciente(pacienteId);
        return ResponseEntity.ok(
            ApiResponse.success(
                "Historial de signos vitales obtenido exitosamente", 
                signosVitales
            )
        );
    }

    @Operation(summary = "Obtener último registro de signos vitales de un paciente")
    @GetMapping("/paciente/{pacienteId}/ultimo")
    public ResponseEntity<ApiResponse<SignosVitalesDTO>> obtenerUltimoRegistro(
            @PathVariable Long pacienteId) {
        log.info("GET /api/v1/signos-vitales/paciente/{}/ultimo - Obteniendo último registro", pacienteId);
        SignosVitalesDTO signosVitales = signosVitalesService.obtenerUltimoRegistro(pacienteId);
        return ResponseEntity.ok(
            ApiResponse.success(
                "Último registro de signos vitales obtenido exitosamente", 
                signosVitales
            )
        );
    }

    @Operation(summary = "Obtener últimos N registros de un paciente")
    @GetMapping("/paciente/{pacienteId}/ultimos")
    public ResponseEntity<ApiResponse<List<SignosVitalesDTO>>> obtenerUltimosRegistros(
            @PathVariable Long pacienteId,
            @RequestParam(defaultValue = "10") int limite) {
        log.info("GET /api/v1/signos-vitales/paciente/{}/ultimos?limite={}", pacienteId, limite);
        List<SignosVitalesDTO> signosVitales = signosVitalesService.obtenerUltimosRegistros(
            pacienteId, 
            limite
        );
        return ResponseEntity.ok(
            ApiResponse.success(
                "Últimos registros obtenidos exitosamente", 
                signosVitales
            )
        );
    }

    @Operation(summary = "Registrar nuevos signos vitales", 
               description = "IMPORTANTE: Si los valores están fuera de rangos normales, se generarán alertas automáticamente")
    @PostMapping
    public ResponseEntity<ApiResponse<SignosVitalesDTO>> registrar(
            @Valid @RequestBody SignosVitalesDTO signosVitalesDTO) {
        log.info("POST /api/v1/signos-vitales - Registrando signos vitales");
        SignosVitalesDTO signosRegistrados = signosVitalesService.registrar(signosVitalesDTO);
        return new ResponseEntity<>(
            ApiResponse.created("Signos vitales registrados exitosamente", signosRegistrados),
            HttpStatus.CREATED
        );
    }

    @Operation(summary = "Actualizar signos vitales existentes")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SignosVitalesDTO>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SignosVitalesDTO signosVitalesDTO) {
        log.info("PUT /api/v1/signos-vitales/{} - Actualizando signos vitales", id);
        SignosVitalesDTO signosActualizados = signosVitalesService.actualizar(id, signosVitalesDTO);
        return ResponseEntity.ok(
            ApiResponse.success("Signos vitales actualizados exitosamente", signosActualizados)
        );
    }

    @Operation(summary = "Eliminar signos vitales")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/v1/signos-vitales/{} - Eliminando signos vitales", id);
        signosVitalesService.eliminar(id);
        return ResponseEntity.ok(
            ApiResponse.success("Signos vitales eliminados exitosamente", null)
        );
    }
}
