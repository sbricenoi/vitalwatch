package com.hospital.vitalwatch.summary.scheduler;

import com.hospital.vitalwatch.summary.model.ResumenDiarioKafka;
import com.hospital.vitalwatch.summary.service.SummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailySummaryScheduler {

    private final SummaryService summaryService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void generateDailySummaryAtMidnight() {
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            
            log.info("🌙 Ejecutando resumen diario automático para {}", yesterday);
            
            ResumenDiarioKafka resumen = summaryService.generateDailySummary(yesterday);
            
            log.info("✅ Resumen diario completado - Mediciones: {} | Alertas: {} | Pacientes: {}",
                resumen.getTotalMediciones(),
                resumen.getTotalAlertas(),
                resumen.getTotalPacientesMonitoreados()
            );
            
        } catch (Exception e) {
            log.error("❌ Error generando resumen diario automático: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 */15 * * * ?")
    public void updateCurrentDaySummary() {
        try {
            LocalDate today = LocalDate.now();
            
            summaryService.generateDailySummary(today);
            
            log.debug("🔄 Resumen del día actual actualizado");
            
        } catch (Exception e) {
            log.error("❌ Error actualizando resumen del día: {}", e.getMessage(), e);
        }
    }
}
