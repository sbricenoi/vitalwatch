package com.hospital.vitalwatch.dbsaver.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.vitalwatch.dbsaver.model.AlertaKafka;
import com.hospital.vitalwatch.dbsaver.repository.AlertaKafkaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertsKafkaConsumer {

    private final AlertaKafkaRepository repository;
    private final ObjectMapper objectMapper;
    
    private final AtomicLong alertsSaved = new AtomicLong(0);

    @KafkaListener(
        topics = "${kafka.topic.alerts}",
        groupId = "${kafka.group-id.alerts}",
        containerFactory = "alertsKafkaListenerContainerFactory"
    )
    public void consumeAlerts(ConsumerRecord<String, String> record) {
        try {
            Map<String, Object> data = objectMapper.readValue(record.value(), Map.class);
            
            String alertId = (String) data.get("alertId");
            
            if (repository.existsByAlertId(alertId)) {
                log.debug("⏭️ Alerta {} ya existe en BD, omitiendo", alertId);
                return;
            }
            
            String anomaliasJson = objectMapper.writeValueAsString(data.get("anomalias"));
            
            AlertaKafka entity = AlertaKafka.builder()
                .kafkaTopic(record.topic())
                .kafkaPartition(record.partition())
                .kafkaOffset(record.offset())
                .kafkaTimestamp(LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(record.timestamp()), 
                    ZoneId.systemDefault()
                ))
                .alertId(alertId)
                .pacienteId((String) data.get("pacienteId"))
                .pacienteNombre((String) data.get("pacienteNombre"))
                .sala((String) data.get("sala"))
                .cama((String) data.get("cama"))
                .tipoAlerta((String) data.get("tipoAlerta"))
                .mensaje((String) data.get("mensaje"))
                .severidad((String) data.get("severidad"))
                .frecuenciaCardiaca((Integer) data.get("frecuenciaCardiaca"))
                .presionSistolica((Integer) data.get("presionSistolica"))
                .presionDiastolica((Integer) data.get("presionDiastolica"))
                .temperatura(data.get("temperatura") != null ? 
                    ((Number) data.get("temperatura")).doubleValue() : null)
                .saturacionOxigeno((Integer) data.get("saturacionOxigeno"))
                .frecuenciaRespiratoria((Integer) data.get("frecuenciaRespiratoria"))
                .anomalias(anomaliasJson)
                .cantidadAnomalias((Integer) data.get("cantidadAnomalias"))
                .deviceId((String) data.get("deviceId"))
                .estado("ACTIVA")
                .detectedAt(LocalDateTime.parse((String) data.get("detectedAt")))
                .build();
            
            repository.save(entity);
            
            long count = alertsSaved.incrementAndGet();
            
            log.warn("🚨 Alerta guardada en Oracle: {} | Paciente: {} | Severidad: {} | Total: {}",
                alertId,
                entity.getPacienteNombre(),
                entity.getSeveridad(),
                count
            );
            
        } catch (Exception e) {
            log.error("❌ Error guardando alerta: {}", e.getMessage(), e);
        }
    }

    public long getAlertsSaved() {
        return alertsSaved.get();
    }
}
