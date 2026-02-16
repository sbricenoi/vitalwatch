package com.hospital.vitalwatch.summary.controller;

import com.hospital.vitalwatch.summary.dto.ApiResponse;
import com.hospital.vitalwatch.summary.dto.SummaryMessage;
import com.hospital.vitalwatch.summary.service.SummaryGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para generación manual de resúmenes
 */
@RestController
@RequestMapping("/api/v1/summary")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Generador de Resúmenes", description = "Productor 2: Genera resúmenes periódicos de signos vitales")
public class SummaryController {

    private final SummaryGeneratorService summaryGeneratorService;

    /**
     * Genera un resumen manualmente (para pruebas)
     */
    @PostMapping("/generate")
    @Operation(
        summary = "Generar resumen manual",
        description = "Genera un resumen de signos vitales manualmente y lo publica a RabbitMQ"
    )
    public ResponseEntity<ApiResponse<SummaryMessage>> generateSummary() {
        log.info("📥 POST /api/v1/summary/generate - Generación manual de resumen");

        try {
            SummaryMessage summary = summaryGeneratorService.generateManualSummary();

            return ResponseEntity.ok(
                ApiResponse.success("Resumen generado y publicado a RabbitMQ", summary)
            );

        } catch (Exception e) {
            log.error("❌ Error generando resumen", e);
            return ResponseEntity
                .internalServerError()
                .body(ApiResponse.error("Error generando resumen: " + e.getMessage()));
        }
    }

    /**
     * Obtiene estadísticas del generador
     */
    @GetMapping("/stats")
    @Operation(summary = "Obtener estadísticas del generador")
    public ResponseEntity<ApiResponse<SummaryGeneratorService.SummaryStats>> getStats() {
        log.info("📥 GET /api/v1/summary/stats");
        
        SummaryGeneratorService.SummaryStats stats = summaryGeneratorService.getStats();
        
        return ResponseEntity.ok(
            ApiResponse.success("Estadísticas obtenidas", stats)
        );
    }

    /**
     * Health check del productor
     */
    @GetMapping("/health")
    @Operation(summary = "Health check del productor")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Summary Generator Producer");
        health.put("timestamp", java.time.LocalDateTime.now().toString());

        return ResponseEntity.ok(
            ApiResponse.success("Productor operativo", health)
        );
    }
}
