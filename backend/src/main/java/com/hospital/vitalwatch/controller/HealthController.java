package com.hospital.vitalwatch.controller;

import com.hospital.vitalwatch.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para verificar el estado de salud de la aplicación
 */
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Health Check", description = "Endpoints para verificar el estado del sistema")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    @Operation(summary = "Verificar estado de la aplicación")
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        log.info("GET /api/v1/health - Verificando estado de la aplicación");
        
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "VitalWatch API");
        health.put("version", "1.0.0");
        
        return ResponseEntity.ok(
            ApiResponse.success("Servicio operativo", health)
        );
    }

    @Operation(summary = "Verificar conexión a base de datos")
    @GetMapping("/database")
    public ResponseEntity<ApiResponse<Map<String, Object>>> databaseCheck() {
        log.info("GET /api/v1/health/database - Verificando conexión a base de datos");
        
        Map<String, Object> dbHealth = new HashMap<>();
        
        try {
            // Ejecutar una consulta simple para verificar la conexión
            String result = jdbcTemplate.queryForObject(
                "SELECT 'OK' FROM DUAL", 
                String.class
            );
            
            dbHealth.put("status", "UP");
            dbHealth.put("database", "Oracle Cloud");
            dbHealth.put("connection", result);
            dbHealth.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(
                ApiResponse.success("Base de datos conectada exitosamente", dbHealth)
            );
            
        } catch (Exception e) {
            log.error("Error al conectar con la base de datos", e);
            
            dbHealth.put("status", "DOWN");
            dbHealth.put("database", "Oracle Cloud");
            dbHealth.put("error", e.getMessage());
            dbHealth.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(503).body(
                ApiResponse.error("Error de conexión a la base de datos", dbHealth, 503)
            );
        }
    }
}
