package com.hospital.vitalwatch.alertprocessor.controller;

import com.hospital.vitalwatch.alertprocessor.consumer.VitalSignsStreamConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/processor")
@RequiredArgsConstructor
public class AlertProcessorController {

    private final VitalSignsStreamConsumer streamConsumer;

    @Value("${kafka.topic.vital-signs}")
    private String vitalSignsTopic;

    @Value("${kafka.topic.alerts}")
    private String alertsTopic;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        long processed = streamConsumer.getMessagesProcessed();
        long alerts = streamConsumer.getAlertsGenerated();
        
        double alertRate = processed > 0 ? (alerts * 100.0 / processed) : 0;
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", "200");
        response.put("message", "Estadísticas del Alert Processor");
        response.put("data", Map.of(
            "messagesProcessed", processed,
            "alertsGenerated", alerts,
            "alertRate", String.format("%.2f%%", alertRate),
            "topicConsuming", vitalSignsTopic,
            "topicProducing", alertsTopic,
            "timestamp", LocalDateTime.now()
        ));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", "200");
        response.put("message", "Alert Processor operativo");
        response.put("data", Map.of(
            "service", "Alert Processor",
            "status", "UP",
            "messagesProcessed", streamConsumer.getMessagesProcessed(),
            "alertsGenerated", streamConsumer.getAlertsGenerated(),
            "timestamp", LocalDateTime.now()
        ));
        
        return ResponseEntity.ok(response);
    }
}
