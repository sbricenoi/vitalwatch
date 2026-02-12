package com.hospital.vitalwatch.anomaly.controller;

import com.hospital.vitalwatch.anomaly.dto.AlertMessage;
import com.hospital.vitalwatch.anomaly.dto.ApiResponse;
import com.hospital.vitalwatch.anomaly.dto.VitalSignsCheckRequest;
import com.hospital.vitalwatch.anomaly.service.AnomalyDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para recepción de signos vitales
 * y detección de anomalías
 */
@RestController
@RequestMapping("/api/v1/vital-signs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Detector de Anomalías", description = "Productor 1: Recibe signos vitales y detecta anomalías")
public class VitalSignsController {

    private final AnomalyDetectionService anomalyDetectionService;

    /**
     * Endpoint para recibir signos vitales de dispositivos médicos
     * y verificar si hay anomalías
     */
    @PostMapping("/check")
    @Operation(
        summary = "Verificar signos vitales",
        description = "Recibe signos vitales de un dispositivo médico, detecta anomalías y publica alertas a RabbitMQ si es necesario"
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkVitalSigns(
            @Valid @RequestBody VitalSignsCheckRequest request) {
        
        log.info("📥 POST /api/v1/vital-signs/check - Paciente: {}", request.getPacienteId());

        try {
            AlertMessage alert = anomalyDetectionService.checkVitalSigns(request);

            Map<String, Object> response = new HashMap<>();
            response.put("pacienteId", request.getPacienteId());
            response.put("pacienteNombre", request.getPacienteNombre());

            if (alert != null) {
                response.put("hasAnomalies", true);
                response.put("anomaliesCount", alert.getAnomalies().size());
                response.put("severity", alert.getSeverity());
                response.put("anomalies", alert.getAnomalies());
                response.put("alertPublished", true);
                response.put("queueName", "vital-signs-alerts");

                return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.created(
                        "Anomalías detectadas. Alerta publicada a RabbitMQ", 
                        response
                    ));
            } else {
                response.put("hasAnomalies", false);
                response.put("anomaliesCount", 0);
                response.put("alertPublished", false);
                response.put("message", "Signos vitales dentro de rangos normales");

                return ResponseEntity
                    .ok(ApiResponse.success(
                        "Signos vitales verificados correctamente", 
                        response
                    ));
            }

        } catch (Exception e) {
            log.error("❌ Error procesando signos vitales", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error procesando signos vitales: " + e.getMessage()));
        }
    }

    /**
     * Health check del productor
     */
    @GetMapping("/health")
    @Operation(summary = "Health check del productor")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Anomaly Detector Producer");
        health.put("timestamp", java.time.LocalDateTime.now().toString());

        return ResponseEntity.ok(
            ApiResponse.success("Productor operativo", health)
        );
    }
}
