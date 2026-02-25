package com.hospital.vitalwatch.alertprocessor.consumer;

import com.hospital.vitalwatch.alertprocessor.dto.AlertMessage;
import com.hospital.vitalwatch.alertprocessor.dto.VitalSignsMessage;
import com.hospital.vitalwatch.alertprocessor.producer.AlertProducer;
import com.hospital.vitalwatch.alertprocessor.service.AnomalyDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class VitalSignsStreamConsumer {

    private final AnomalyDetectionService anomalyDetectionService;
    private final AlertProducer alertProducer;
    
    private final AtomicLong messagesProcessed = new AtomicLong(0);
    private final AtomicLong alertsGenerated = new AtomicLong(0);

    @KafkaListener(
        topics = "${kafka.topic.vital-signs}",
        groupId = "${kafka.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(VitalSignsMessage message) {
        try {
            long count = messagesProcessed.incrementAndGet();
            
            AlertMessage alert = anomalyDetectionService.detectAnomalies(message);
            
            if (alert != null) {
                alertProducer.publishAlert(alert);
                long alertCount = alertsGenerated.incrementAndGet();
                
                log.warn("🚨 Alerta generada #{} - Paciente: {} - Severidad: {} - {} anomalías",
                    alertCount,
                    message.getPacienteNombre(),
                    alert.getSeveridad(),
                    alert.getCantidadAnomalias()
                );
            }
            
            if (count % 60 == 0) {
                log.info("📊 Stream procesado: {} mensajes | Alertas: {} ({} %)",
                    count,
                    alertsGenerated.get(),
                    String.format("%.2f", (alertsGenerated.get() * 100.0 / count))
                );
            }
            
        } catch (Exception e) {
            log.error("❌ Error procesando mensaje: {}", e.getMessage(), e);
        }
    }

    public long getMessagesProcessed() {
        return messagesProcessed.get();
    }

    public long getAlertsGenerated() {
        return alertsGenerated.get();
    }
}
