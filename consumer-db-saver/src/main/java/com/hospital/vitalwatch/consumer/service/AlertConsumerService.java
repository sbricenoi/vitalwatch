package com.hospital.vitalwatch.consumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.vitalwatch.consumer.dto.AlertMessage;
import com.hospital.vitalwatch.consumer.entity.AlertaMQ;
import com.hospital.vitalwatch.consumer.repository.AlertaMQRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio consumidor que recibe alertas de RabbitMQ
 * y las guarda en Oracle Cloud
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertConsumerService {

    private final AlertaMQRepository alertaMQRepository;
    private final ObjectMapper objectMapper;

    private long messagesProcessed = 0;
    private long messagesFailed = 0;

    /**
     * Listener que consume mensajes de la cola vital-signs-alerts
     */
    @RabbitListener(queues = "${rabbitmq.queue.alerts}")
    @Transactional
    public void consumeAlert(AlertMessage alertMessage) {
        try {
            log.info("📥 Alerta recibida desde RabbitMQ: Paciente {} - Severidad: {} - {} anomalías",
                    alertMessage.getPacienteId(),
                    alertMessage.getSeverity(),
                    alertMessage.getAnomalies().size());

            // Convertir a entidad y guardar
            AlertaMQ alerta = convertToEntity(alertMessage);
            AlertaMQ savedAlert = alertaMQRepository.save(alerta);

            messagesProcessed++;

            log.info("✅ Alerta guardada en Oracle con ID: {} - Total procesadas: {}",
                    savedAlert.getId(), messagesProcessed);

        } catch (Exception e) {
            messagesFailed++;
            log.error("❌ Error procesando alerta: {} - Total fallidas: {}",
                    e.getMessage(), messagesFailed, e);
            // En producción, aquí podrías implementar:
            // - Dead Letter Queue
            // - Retry logic
            // - Alertas de monitoreo
        }
    }

    /**
     * Convierte AlertMessage a entidad AlertaMQ
     */
    private AlertaMQ convertToEntity(AlertMessage message) throws JsonProcessingException {
        AlertaMQ alerta = new AlertaMQ();

        alerta.setPacienteId(message.getPacienteId());
        alerta.setPacienteNombre(message.getPacienteNombre());
        alerta.setSala(message.getSala());
        alerta.setCama(message.getCama());

        // Serializar anomalías a JSON
        String anomaliasJson = objectMapper.writeValueAsString(message.getAnomalies());
        alerta.setAnomaliasJson(anomaliasJson);
        alerta.setAnomaliasCount(message.getAnomalies().size());

        alerta.setSeverity(message.getSeverity());
        alerta.setDeviceId(message.getDeviceId());
        alerta.setDetectedAt(message.getDetectedAt());
        alerta.setSavedAt(LocalDateTime.now());
        alerta.setQueueName("vital-signs-alerts");

        // Extraer signos vitales
        if (message.getVitalSigns() != null) {
            alerta.setFrecuenciaCardiaca(message.getVitalSigns().getFrecuenciaCardiaca());
            alerta.setPresionSistolica(message.getVitalSigns().getPresionSistolica());
            alerta.setPresionDiastolica(message.getVitalSigns().getPresionDiastolica());
            alerta.setTemperatura(message.getVitalSigns().getTemperatura());
            alerta.setSaturacionOxigeno(message.getVitalSigns().getSaturacionOxigeno());
            alerta.setFrecuenciaRespiratoria(message.getVitalSigns().getFrecuenciaRespiratoria());
        }

        return alerta;
    }

    /**
     * Obtiene estadísticas del consumidor
     */
    public ConsumerStats getStats() {
        long totalInDb = alertaMQRepository.count();
        return new ConsumerStats(messagesProcessed, messagesFailed, totalInDb);
    }

    public static class ConsumerStats {
        private long messagesProcessed;
        private long messagesFailed;
        private long totalAlertsInDatabase;

        public ConsumerStats(long messagesProcessed, long messagesFailed, long totalAlertsInDatabase) {
            this.messagesProcessed = messagesProcessed;
            this.messagesFailed = messagesFailed;
            this.totalAlertsInDatabase = totalAlertsInDatabase;
        }

        public long getMessagesProcessed() {
            return messagesProcessed;
        }

        public long getMessagesFailed() {
            return messagesFailed;
        }

        public long getTotalAlertsInDatabase() {
            return totalAlertsInDatabase;
        }
    }
}
