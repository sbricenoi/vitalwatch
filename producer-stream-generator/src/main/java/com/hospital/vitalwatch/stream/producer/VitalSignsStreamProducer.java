package com.hospital.vitalwatch.stream.producer;

import com.hospital.vitalwatch.stream.dto.VitalSignsMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class VitalSignsStreamProducer {

    private final KafkaTemplate<String, VitalSignsMessage> kafkaTemplate;
    private final String topicName;
    private final AtomicLong messageCounter = new AtomicLong(0);

    public VitalSignsStreamProducer(
            KafkaTemplate<String, VitalSignsMessage> kafkaTemplate,
            @Value("${kafka.topic.vital-signs}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    public void publishVitalSigns(VitalSignsMessage message) {
        try {
            String key = message.getPacienteId();
            
            CompletableFuture<SendResult<String, VitalSignsMessage>> future = 
                kafkaTemplate.send(topicName, key, message);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    long count = messageCounter.incrementAndGet();
                    
                    if (count % 10 == 0) {
                        log.info("✅ Stream: {} mensajes publicados | Último: {} - FC:{} | Tópico: {} | Partición: {} | Offset: {}",
                            count,
                            message.getPacienteNombre(),
                            message.getFrecuenciaCardiaca(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                        );
                    }
                } else {
                    log.error("❌ Error publicando mensaje a Kafka: {}", ex.getMessage());
                }
            });
            
        } catch (Exception e) {
            log.error("❌ Error al enviar mensaje: {}", e.getMessage(), e);
        }
    }

    public long getMessageCount() {
        return messageCounter.get();
    }

    public String getTopicName() {
        return topicName;
    }
}
