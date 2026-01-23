package com.hospital.vitalwatch.controller;

import com.hospital.vitalwatch.dto.AlertaDTO;
import com.hospital.vitalwatch.dto.ApiResponse;
import com.hospital.vitalwatch.dto.PacienteDTO;
import com.hospital.vitalwatch.service.AlertaService;
import com.hospital.vitalwatch.service.PacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para Dashboard y Estadísticas
 * Proporciona endpoints para obtener información general del sistema
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard", description = "API para obtener estadísticas y vista general del sistema")
public class DashboardController {

    private final PacienteService pacienteService;
    private final AlertaService alertaService;

    @Operation(summary = "Obtener estadísticas generales del dashboard")
    @GetMapping("/estadisticas")
    public ResponseEntity<ApiResponse<Map<String, Object>>> obtenerEstadisticas() {
        log.info("GET /api/v1/dashboard/estadisticas - Obteniendo estadísticas generales");
        
        Map<String, Object> estadisticas = new HashMap<>();
        
        // Obtener estadísticas de pacientes
        List<PacienteDTO> todosPacientes = pacienteService.obtenerTodos();
        List<PacienteDTO> pacientesCriticos = pacienteService.obtenerPacientesCriticos();
        
        estadisticas.put("totalPacientes", todosPacientes.size());
        estadisticas.put("pacientesCriticos", pacientesCriticos.size());
        
        // Obtener estadísticas de alertas
        Map<String, Long> estadisticasAlertas = alertaService.obtenerEstadisticas();
        estadisticas.put("alertasActivas", estadisticasAlertas.get("alertasActivas"));
        estadisticas.put("alertasCriticas", estadisticasAlertas.get("alertasCriticas"));
        
        // Calcular porcentajes
        if (todosPacientes.size() > 0) {
            double porcentajeCriticos = (pacientesCriticos.size() * 100.0) / todosPacientes.size();
            estadisticas.put("porcentajePacientesCriticos", 
                            Math.round(porcentajeCriticos * 100.0) / 100.0);
        } else {
            estadisticas.put("porcentajePacientesCriticos", 0.0);
        }
        
        return ResponseEntity.ok(
            ApiResponse.success("Estadísticas obtenidas exitosamente", estadisticas)
        );
    }

    @Operation(summary = "Obtener resumen de pacientes por estado")
    @GetMapping("/pacientes-por-estado")
    public ResponseEntity<ApiResponse<Map<String, Long>>> obtenerPacientesPorEstado() {
        log.info("GET /api/v1/dashboard/pacientes-por-estado - Obteniendo resumen por estado");
        
        Map<String, Long> resumen = new HashMap<>();
        
        // Obtener estadísticas por estado
        List<Object[]> estadisticas = pacienteService.obtenerEstadisticas();
        
        for (Object[] stat : estadisticas) {
            String estado = (String) stat[0];
            Long cantidad = ((Number) stat[1]).longValue();
            resumen.put(estado, cantidad);
        }
        
        // Asegurar que todos los estados estén presentes
        resumen.putIfAbsent("ESTABLE", 0L);
        resumen.putIfAbsent("MODERADO", 0L);
        resumen.putIfAbsent("CRÍTICO", 0L);
        resumen.putIfAbsent("RECUPERACIÓN", 0L);
        
        return ResponseEntity.ok(
            ApiResponse.success("Resumen por estado obtenido exitosamente", resumen)
        );
    }

    @Operation(summary = "Obtener alertas recientes para el dashboard")
    @GetMapping("/alertas-recientes")
    public ResponseEntity<ApiResponse<List<AlertaDTO>>> obtenerAlertasRecientes(
            @RequestParam(defaultValue = "10") int limite) {
        log.info("GET /api/v1/dashboard/alertas-recientes?limite={}", limite);
        
        List<AlertaDTO> alertas = alertaService.obtenerRecientes(limite);
        
        return ResponseEntity.ok(
            ApiResponse.success("Alertas recientes obtenidas exitosamente", alertas)
        );
    }

    @Operation(summary = "Obtener pacientes críticos para el dashboard")
    @GetMapping("/pacientes-criticos")
    public ResponseEntity<ApiResponse<List<PacienteDTO>>> obtenerPacientesCriticos() {
        log.info("GET /api/v1/dashboard/pacientes-criticos - Obteniendo pacientes críticos");
        
        List<PacienteDTO> pacientes = pacienteService.obtenerPacientesCriticos();
        
        return ResponseEntity.ok(
            ApiResponse.success("Pacientes críticos obtenidos exitosamente", pacientes)
        );
    }

    @Operation(summary = "Obtener resumen de alertas por severidad")
    @GetMapping("/alertas-por-severidad")
    public ResponseEntity<ApiResponse<Map<String, Long>>> obtenerAlertasPorSeveridad() {
        log.info("GET /api/v1/dashboard/alertas-por-severidad - Obteniendo resumen por severidad");
        
        Map<String, Long> resumen = new HashMap<>();
        
        // Obtener estadísticas por severidad
        List<Object[]> estadisticas = alertaService.obtenerEstadisticasPorSeveridad();
        
        for (Object[] stat : estadisticas) {
            String severidad = (String) stat[0];
            Long cantidad = ((Number) stat[1]).longValue();
            resumen.put(severidad, cantidad);
        }
        
        // Asegurar que todas las severidades estén presentes
        resumen.putIfAbsent("BAJA", 0L);
        resumen.putIfAbsent("MODERADA", 0L);
        resumen.putIfAbsent("CRÍTICA", 0L);
        
        return ResponseEntity.ok(
            ApiResponse.success("Resumen por severidad obtenido exitosamente", resumen)
        );
    }
}
