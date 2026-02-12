package com.hospital.vitalwatch.summary.service;

import com.hospital.vitalwatch.summary.dto.SummaryMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Servicio que genera resúmenes periódicos de signos vitales
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SummaryGeneratorService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.queue.summary}")
    private String summaryQueue;

    private final Random random = new Random();
    private int summaryCount = 0;

    /**
     * Genera resumen cada 5 minutos
     * Para pruebas, se puede configurar con menor intervalo
     */
    @Scheduled(fixedDelayString = "${summary.interval.ms:300000}") // 5 minutos por defecto
    public void generatePeriodicSummary() {
        summaryCount++;
        
        log.info("📊 Generando resumen periódico #{}", summaryCount);

        SummaryMessage summary = buildSummary();
        publishSummary(summary);
    }

    /**
     * Genera resumen manualmente (para pruebas)
     */
    public SummaryMessage generateManualSummary() {
        log.info("📊 Generando resumen manual");
        SummaryMessage summary = buildSummary();
        publishSummary(summary);
        return summary;
    }

    private SummaryMessage buildSummary() {
        SummaryMessage summary = new SummaryMessage();
        summary.setTimestamp(LocalDateTime.now());
        summary.setSummaryType("PERIODIC_SUMMARY");
        
        // Simular estadísticas (en producción, estos datos vendrían de una BD o cache)
        summary.setTotalPacientes(9);
        summary.setPacientesCriticos(3);
        summary.setPacientesMonitoreados(9);
        summary.setAlertasGeneradas(random.nextInt(10) + 5); // 5-15
        summary.setAlertasCriticas(random.nextInt(5) + 1);   // 1-6
        
        // Promedios simulados
        summary.setPromedioFrecuenciaCardiaca(75.0 + (random.nextDouble() * 10));
        summary.setPromedioTemperatura(36.5 + (random.nextDouble() * 0.8));
        summary.setPromedioSaturacionOxigeno(95.0 + (random.nextDouble() * 5));

        // Lista de pacientes con su estado
        List<SummaryMessage.PacienteStatus> pacientes = new ArrayList<>();
        
        pacientes.add(new SummaryMessage.PacienteStatus(
            1L, "Juan Pérez", "UCI", "A-01", "CRÍTICO", 
            2, LocalDateTime.now().minusMinutes(5)
        ));
        
        pacientes.add(new SummaryMessage.PacienteStatus(
            2L, "María González", "UCI", "A-02", "ESTABLE", 
            0, LocalDateTime.now().minusMinutes(3)
        ));
        
        pacientes.add(new SummaryMessage.PacienteStatus(
            3L, "Pedro López", "UCI", "B-01", "MODERADO", 
            1, LocalDateTime.now().minusMinutes(7)
        ));

        summary.setPacientesStatus(pacientes);

        log.debug("📊 Resumen generado: {} pacientes, {} alertas", 
                summary.getTotalPacientes(), summary.getAlertasGeneradas());

        return summary;
    }

    private void publishSummary(SummaryMessage summary) {
        try {
            rabbitTemplate.convertAndSend(summaryQueue, summary);
            log.info("📤 Resumen publicado a RabbitMQ: {} pacientes, {} alertas críticas",
                    summary.getTotalPacientes(), summary.getAlertasCriticas());
        } catch (Exception e) {
            log.error("❌ Error publicando resumen a RabbitMQ", e);
            throw new RuntimeException("Error al publicar resumen: " + e.getMessage());
        }
    }

    /**
     * Obtiene estadísticas del generador
     */
    public SummaryStats getStats() {
        return new SummaryStats(summaryCount, LocalDateTime.now());
    }

    @Data
    @AllArgsConstructor
    public static class SummaryStats {
        private int totalSummariesGenerated;
        private LocalDateTime lastCheck;
    }
}
