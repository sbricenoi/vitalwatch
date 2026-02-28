package com.hospital.vitalwatch.alertprocessor.producer;

import com.hospital.vitalwatch.alertprocessor.dto.AlertMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class AlertProducer {

    private final KafkaTemplate<String, AlertMessage> kafkaTemplate;
    private final String topicName;

    public AlertProducer(
            KafkaTemplate<String, AlertMessage> kafkaTemplate,
            @Value("${kafka.topic.alerts}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    public void publishAlert(AlertMessage alert) {
        try {
            String key = alert.getPacienteId();
            
            CompletableFuture<SendResult<String, AlertMessage>> future = 
                kafkaTemplate.send(topicName, key, alert);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("✅ Alerta publicada - ID: {} | Paciente: {} | Severidad: {} | Partición: {} | Offset: {}",
                        alert.getAlertId(),
                        alert.getPacienteNombre(),
                        alert.getSeveridad(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                    );
                } else {
                    log.error("❌ Error publicando alerta: {}", ex.getMessage());
                }
            });
            
        } catch (Exception e) {
            log.error("❌ Error al enviar alerta: {}", e.getMessage(), e);
        }
    }
}
