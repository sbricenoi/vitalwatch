package com.hospital.vitalwatch.dbsaver.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.vitalwatch.dbsaver.model.SignosVitalesKafka;
import com.hospital.vitalwatch.dbsaver.repository.SignosVitalesKafkaRepository;
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
public class VitalSignsKafkaConsumer {

    private final SignosVitalesKafkaRepository repository;
    private final ObjectMapper objectMapper;
    
    private final AtomicLong messagesSaved = new AtomicLong(0);

    @KafkaListener(
        topics = "${kafka.topic.vital-signs}",
        groupId = "${kafka.group-id.vital-signs}",
        containerFactory = "vitalSignsKafkaListenerContainerFactory"
    )
    public void consumeVitalSigns(ConsumerRecord<String, String> record) {
        try {
            Map<String, Object> data = objectMapper.readValue(record.value(), Map.class);
            
            SignosVitalesKafka entity = SignosVitalesKafka.builder()
                .kafkaTopic(record.topic())
                .kafkaPartition(record.partition())
                .kafkaOffset(record.offset())
                .kafkaTimestamp(LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(record.timestamp()), 
                    ZoneId.systemDefault()
                ))
                .pacienteId((String) data.get("pacienteId"))
                .pacienteNombre((String) data.get("pacienteNombre"))
                .sala((String) data.get("sala"))
                .cama((String) data.get("cama"))
                .frecuenciaCardiaca((Integer) data.get("frecuenciaCardiaca"))
                .presionSistolica((Integer) data.get("presionSistolica"))
                .presionDiastolica((Integer) data.get("presionDiastolica"))
                .temperatura(data.get("temperatura") != null ? 
                    ((Number) data.get("temperatura")).doubleValue() : null)
                .saturacionOxigeno((Integer) data.get("saturacionOxigeno"))
                .frecuenciaRespiratoria((Integer) data.get("frecuenciaRespiratoria"))
                .deviceId((String) data.get("deviceId"))
                .timestampMedicion(LocalDateTime.parse((String) data.get("timestamp")))
                .build();
            
            repository.save(entity);
            
            long count = messagesSaved.incrementAndGet();
            
            if (count % 60 == 0) {
                log.info("💾 Signos vitales guardados en Oracle: {} registros | Último: {} - FC: {}",
                    count,
                    entity.getPacienteNombre(),
                    entity.getFrecuenciaCardiaca()
                );
            }
            
        } catch (Exception e) {
            log.error("❌ Error guardando signos vitales: {}", e.getMessage(), e);
        }
    }

    public long getMessagesSaved() {
        return messagesSaved.get();
    }
}
