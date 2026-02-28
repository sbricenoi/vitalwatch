package com.hospital.vitalwatch.summary.controller;

import com.hospital.vitalwatch.summary.model.ResumenDiarioKafka;
import com.hospital.vitalwatch.summary.repository.ResumenDiarioRepository;
import com.hospital.vitalwatch.summary.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/summary")
@RequiredArgsConstructor
public class SummaryController {

    private final SummaryService summaryService;
    private final ResumenDiarioRepository resumenRepository;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        
        if (fecha == null) {
            fecha = LocalDate.now();
        }
        
        ResumenDiarioKafka resumen = summaryService.generateDailySummary(fecha);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", "201");
        response.put("message", "Resumen diario generado exitosamente");
        response.put("data", Map.of(
            "fecha", resumen.getFecha(),
            "totalPacientes", resumen.getTotalPacientesMonitoreados(),
            "totalMediciones", resumen.getTotalMediciones(),
            "totalAlertas", resumen.getTotalAlertas(),
            "alertasCriticas", resumen.getAlertasCriticas(),
            "promedioFC", resumen.getPromedioFrecuenciaCardiaca(),
            "promedioTemp", resumen.getPromedioTemperatura(),
            "promedioSpO2", resumen.getPromedioSaturacionOxigeno()
        ));
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/daily/{fecha}")
    public ResponseEntity<Map<String, Object>> getDailySummary(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        
        return resumenRepository.findByFecha(fecha)
            .map(resumen -> {
                Map<String, Object> response = new HashMap<>();
                response.put("code", "200");
                response.put("message", "Resumen diario encontrado");
                response.put("data", resumen);
                return ResponseEntity.ok(response);
            })
            .orElseGet(() -> {
                Map<String, Object> response = new HashMap<>();
                response.put("code", "404");
                response.put("message", "No hay resumen para la fecha " + fecha);
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            });
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllSummaries() {
        List<ResumenDiarioKafka> resumenes = resumenRepository.findAll();
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", "200");
        response.put("message", "Resúmenes diarios obtenidos");
        response.put("data", Map.of(
            "total", resumenes.size(),
            "summaries", resumenes
        ));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", "200");
        response.put("message", "Summary Generator operativo");
        response.put("data", Map.of(
            "service", "Summary Generator",
            "status", "UP",
            "timestamp", LocalDateTime.now()
        ));
        
        return ResponseEntity.ok(response);
    }
}
