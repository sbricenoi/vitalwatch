package com.hospital.vitalwatch.controller;

import com.hospital.vitalwatch.dto.AlertaDTO;
import com.hospital.vitalwatch.dto.ApiResponse;
import com.hospital.vitalwatch.service.AlertaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de Alertas
 * Expone endpoints para gestión de alertas médicas
 */
@RestController
@RequestMapping("/api/v1/alertas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Alertas", description = "API para gestión de alertas médicas")
public class AlertaController {

    private final AlertaService alertaService;

    @Operation(summary = "Listar todas las alertas")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AlertaDTO>>> obtenerTodas() {
        log.info("GET /api/v1/alertas - Obteniendo todas las alertas");
        List<AlertaDTO> alertas = alertaService.obtenerTodas();
        return ResponseEntity.ok(
            ApiResponse.success("Alertas obtenidas exitosamente", alertas)
        );
    }

    @Operation(summary = "Obtener alerta por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AlertaDTO>> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/v1/alertas/{} - Obteniendo alerta", id);
        AlertaDTO alerta = alertaService.obtenerPorId(id);
        return ResponseEntity.ok(
            ApiResponse.success("Alerta obtenida exitosamente", alerta)
        );
    }

    @Operation(summary = "Listar alertas activas")
    @GetMapping("/activas")
    public ResponseEntity<ApiResponse<List<AlertaDTO>>> obtenerActivas() {
        log.info("GET /api/v1/alertas/activas - Obteniendo alertas activas");
        List<AlertaDTO> alertas = alertaService.obtenerActivas();
        return ResponseEntity.ok(
            ApiResponse.success("Alertas activas obtenidas exitosamente", alertas)
        );
    }

    @Operation(summary = "Listar alertas críticas activas")
    @GetMapping("/criticas")
    public ResponseEntity<ApiResponse<List<AlertaDTO>>> obtenerCriticasActivas() {
        log.info("GET /api/v1/alertas/criticas - Obteniendo alertas críticas activas");
        List<AlertaDTO> alertas = alertaService.obtenerCriticasActivas();
        return ResponseEntity.ok(
            ApiResponse.success("Alertas críticas obtenidas exitosamente", alertas)
        );
    }

    @Operation(summary = "Obtener alertas de un paciente")
    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<ApiResponse<List<AlertaDTO>>> obtenerPorPaciente(
            @PathVariable Long pacienteId) {
        log.info("GET /api/v1/alertas/paciente/{} - Obteniendo alertas del paciente", pacienteId);
        List<AlertaDTO> alertas = alertaService.obtenerPorPaciente(pacienteId);
        return ResponseEntity.ok(
            ApiResponse.success("Alertas del paciente obtenidas exitosamente", alertas)
        );
    }

    @Operation(summary = "Obtener alertas activas de un paciente")
    @GetMapping("/paciente/{pacienteId}/activas")
    public ResponseEntity<ApiResponse<List<AlertaDTO>>> obtenerActivasPorPaciente(
            @PathVariable Long pacienteId) {
        log.info("GET /api/v1/alertas/paciente/{}/activas - Obteniendo alertas activas", pacienteId);
        List<AlertaDTO> alertas = alertaService.obtenerActivasPorPaciente(pacienteId);
        return ResponseEntity.ok(
            ApiResponse.success("Alertas activas del paciente obtenidas exitosamente", alertas)
        );
    }

    @Operation(summary = "Obtener alertas por severidad")
    @GetMapping("/severidad/{severidad}")
    public ResponseEntity<ApiResponse<List<AlertaDTO>>> obtenerPorSeveridad(
            @PathVariable String severidad) {
        log.info("GET /api/v1/alertas/severidad/{} - Obteniendo alertas por severidad", severidad);
        List<AlertaDTO> alertas = alertaService.obtenerPorSeveridad(severidad);
        return ResponseEntity.ok(
            ApiResponse.success(
                "Alertas con severidad " + severidad + " obtenidas exitosamente", 
                alertas
            )
        );
    }

    @Operation(summary = "Obtener alertas recientes")
    @GetMapping("/recientes")
    public ResponseEntity<ApiResponse<List<AlertaDTO>>> obtenerRecientes(
            @RequestParam(defaultValue = "10") int limite) {
        log.info("GET /api/v1/alertas/recientes?limite={}", limite);
        List<AlertaDTO> alertas = alertaService.obtenerRecientes(limite);
        return ResponseEntity.ok(
            ApiResponse.success("Alertas recientes obtenidas exitosamente", alertas)
        );
    }

    @Operation(summary = "Crear alerta manual")
    @PostMapping
    public ResponseEntity<ApiResponse<AlertaDTO>> crear(
            @Valid @RequestBody AlertaDTO alertaDTO) {
        log.info("POST /api/v1/alertas - Creando alerta manual");
        AlertaDTO alertaCreada = alertaService.crear(alertaDTO);
        return new ResponseEntity<>(
            ApiResponse.created("Alerta creada exitosamente", alertaCreada),
            HttpStatus.CREATED
        );
    }

    @Operation(summary = "Resolver alerta")
    @PutMapping("/{id}/resolver")
    public ResponseEntity<ApiResponse<AlertaDTO>> resolver(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        log.info("PUT /api/v1/alertas/{}/resolver - Resolviendo alerta", id);
        
        String resueltoPor = body.get("resueltoPor");
        String notasResolucion = body.get("notasResolucion");
        
        AlertaDTO alertaResuelta = alertaService.resolver(id, resueltoPor, notasResolucion);
        return ResponseEntity.ok(
            ApiResponse.success("Alerta resuelta exitosamente", alertaResuelta)
        );
    }

    @Operation(summary = "Descartar alerta")
    @PutMapping("/{id}/descartar")
    public ResponseEntity<ApiResponse<AlertaDTO>> descartar(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        log.info("PUT /api/v1/alertas/{}/descartar - Descartando alerta", id);
        
        String descartadoPor = body.get("resueltoPor");
        String motivo = body.get("notasResolucion");
        
        AlertaDTO alertaDescartada = alertaService.descartar(id, descartadoPor, motivo);
        return ResponseEntity.ok(
            ApiResponse.success("Alerta descartada exitosamente", alertaDescartada)
        );
    }

    @Operation(summary = "Eliminar alerta")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/v1/alertas/{} - Eliminando alerta", id);
        alertaService.eliminar(id);
        return ResponseEntity.ok(
            ApiResponse.success("Alerta eliminada exitosamente", null)
        );
    }

    @Operation(summary = "Obtener estadísticas de alertas")
    @GetMapping("/estadisticas")
    public ResponseEntity<ApiResponse<Map<String, Long>>> obtenerEstadisticas() {
        log.info("GET /api/v1/alertas/estadisticas - Obteniendo estadísticas");
        Map<String, Long> estadisticas = alertaService.obtenerEstadisticas();
        return ResponseEntity.ok(
            ApiResponse.success("Estadísticas obtenidas exitosamente", estadisticas)
        );
    }
}
