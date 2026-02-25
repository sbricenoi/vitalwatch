package com.hospital.vitalwatch.stream.controller;

import com.hospital.vitalwatch.stream.dto.VitalSignsMessage;
import com.hospital.vitalwatch.stream.producer.VitalSignsStreamProducer;
import com.hospital.vitalwatch.stream.scheduler.VitalSignsScheduler;
import com.hospital.vitalwatch.stream.service.VitalSignsGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/stream")
@RequiredArgsConstructor
public class StreamController {

    private final VitalSignsScheduler scheduler;
    private final VitalSignsGeneratorService generatorService;
    private final VitalSignsStreamProducer streamProducer;

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startStream() {
        log.info("▶️ Solicitud para iniciar stream");
        
        scheduler.enableStream();
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", "200");
        response.put("message", "Stream de signos vitales iniciado");
        response.put("data", Map.of(
            "status", "RUNNING",
            "intervalMs", 1000,
            "topic", streamProducer.getTopicName(),
            "timestamp", LocalDateTime.now()
        ));
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopStream() {
        log.info("⏸️ Solicitud para pausar stream");
        
        scheduler.disableStream();
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", "200");
        response.put("message", "Stream de signos vitales pausado");
        response.put("data", Map.of(
            "status", "PAUSED",
            "totalMessagesSent", streamProducer.getMessageCount(),
            "timestamp", LocalDateTime.now()
        ));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStreamStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", "200");
        response.put("message", "Estado del stream");
        response.put("data", Map.of(
            "enabled", scheduler.isStreamEnabled(),
            "status", scheduler.isStreamEnabled() ? "RUNNING" : "PAUSED",
            "totalMessagesSent", streamProducer.getMessageCount(),
            "topic", streamProducer.getTopicName(),
            "intervalMs", 1000,
            "timestamp", LocalDateTime.now()
        ));
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-manual")
    public ResponseEntity<Map<String, Object>> sendManualMessage() {
        log.info("📤 Enviando mensaje manual");
        
        VitalSignsMessage message = generatorService.generateRandomVitalSigns();
        streamProducer.publishVitalSigns(message);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", "201");
        response.put("message", "Mensaje enviado manualmente a Kafka");
        response.put("data", Map.of(
            "messageId", message.getMessageId(),
            "pacienteId", message.getPacienteId(),
            "pacienteNombre", message.getPacienteNombre(),
            "frecuenciaCardiaca", message.getFrecuenciaCardiaca(),
            "temperatura", message.getTemperatura(),
            "saturacionOxigeno", message.getSaturacionOxigeno(),
            "timestamp", message.getTimestamp()
        ));
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", "200");
        response.put("message", "Estadísticas del stream generator");
        response.put("data", Map.of(
            "totalMessagesSent", streamProducer.getMessageCount(),
            "streamEnabled", scheduler.isStreamEnabled(),
            "topic", streamProducer.getTopicName(),
            "intervalMs", 1000,
            "messagesPerMinute", 60,
            "messagesPerHour", 3600,
            "timestamp", LocalDateTime.now()
        ));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", "200");
        response.put("message", "Productor Stream Generator operativo");
        response.put("data", Map.of(
            "service", "Vital Signs Stream Generator",
            "status", "UP",
            "streamEnabled", scheduler.isStreamEnabled(),
            "messagesSent", streamProducer.getMessageCount(),
            "timestamp", LocalDateTime.now()
        ));
        
        return ResponseEntity.ok(response);
    }
}
