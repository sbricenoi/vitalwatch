package com.hospital.vitalwatch.anomaly.service;

import com.hospital.vitalwatch.anomaly.dto.AlertMessage;
import com.hospital.vitalwatch.anomaly.dto.VitalSignsCheckRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio que detecta anomalías en signos vitales
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnomalyDetectionService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.queue.alerts}")
    private String alertsQueue;

    // Rangos normales de signos vitales
    private static final int FC_MIN = 60;
    private static final int FC_MAX = 100;
    private static final int FC_CRITICAL_MIN = 40;
    private static final int FC_CRITICAL_MAX = 120;

    private static final int PS_MIN = 90;
    private static final int PS_MAX = 120;
    private static final int PS_CRITICAL_MIN = 70;
    private static final int PS_CRITICAL_MAX = 160;

    private static final int PD_MIN = 60;
    private static final int PD_MAX = 80;
    private static final int PD_CRITICAL_MIN = 40;
    private static final int PD_CRITICAL_MAX = 100;

    private static final BigDecimal TEMP_MIN = new BigDecimal("36.0");
    private static final BigDecimal TEMP_MAX = new BigDecimal("37.5");
    private static final BigDecimal TEMP_CRITICAL_MIN = new BigDecimal("35.0");
    private static final BigDecimal TEMP_CRITICAL_MAX = new BigDecimal("39.5");

    private static final int SPO2_MIN = 95;
    private static final int SPO2_CRITICAL = 90;

    private static final int FR_MIN = 12;
    private static final int FR_MAX = 20;
    private static final int FR_CRITICAL_MIN = 8;
    private static final int FR_CRITICAL_MAX = 25;

    /**
     * Verifica signos vitales y detecta anomalías
     * Si encuentra anomalías, publica mensaje a RabbitMQ
     */
    public AlertMessage checkVitalSigns(VitalSignsCheckRequest request) {
        log.info("🔍 Verificando signos vitales del paciente {} ({})", 
                request.getPacienteId(), request.getPacienteNombre());

        List<AlertMessage.Anomaly> anomalies = new ArrayList<>();

        // Verificar Frecuencia Cardíaca
        checkFrecuenciaCardiaca(request.getFrecuenciaCardiaca(), anomalies);

        // Verificar Presión Arterial
        checkPresionArterial(request.getPresionSistolica(), request.getPresionDiastolica(), anomalies);

        // Verificar Temperatura
        checkTemperatura(request.getTemperatura(), anomalies);

        // Verificar Saturación de Oxígeno
        checkSaturacionOxigeno(request.getSaturacionOxigeno(), anomalies);

        // Verificar Frecuencia Respiratoria
        checkFrecuenciaRespiratoria(request.getFrecuenciaRespiratoria(), anomalies);

        // Si hay anomalías, publicar a RabbitMQ
        if (!anomalies.isEmpty()) {
            AlertMessage alert = buildAlertMessage(request, anomalies);
            publishAlert(alert);
            return alert;
        }

        log.info("✅ Signos vitales normales - No se generaron alertas");
        return null;
    }

    private void checkFrecuenciaCardiaca(Integer fc, List<AlertMessage.Anomaly> anomalies) {
        if (fc < FC_CRITICAL_MIN || fc > FC_CRITICAL_MAX) {
            anomalies.add(new AlertMessage.Anomaly(
                "CRITICA",
                "Frecuencia Cardíaca",
                fc + " lpm",
                FC_MIN + "-" + FC_MAX + " lpm",
                String.format("Frecuencia cardíaca de %d lpm está en rango crítico", fc)
            ));
        } else if (fc < FC_MIN || fc > FC_MAX) {
            anomalies.add(new AlertMessage.Anomaly(
                "ALTA",
                "Frecuencia Cardíaca",
                fc + " lpm",
                FC_MIN + "-" + FC_MAX + " lpm",
                String.format("Frecuencia cardíaca de %d lpm fuera de rango normal", fc)
            ));
        }
    }

    private void checkPresionArterial(Integer ps, Integer pd, List<AlertMessage.Anomaly> anomalies) {
        if (ps < PS_CRITICAL_MIN || ps > PS_CRITICAL_MAX) {
            anomalies.add(new AlertMessage.Anomaly(
                "CRITICA",
                "Presión Sistólica",
                ps + " mmHg",
                PS_MIN + "-" + PS_MAX + " mmHg",
                String.format("Presión sistólica de %d mmHg está en rango crítico", ps)
            ));
        } else if (ps < PS_MIN || ps > PS_MAX) {
            anomalies.add(new AlertMessage.Anomaly(
                "ALTA",
                "Presión Sistólica",
                ps + " mmHg",
                PS_MIN + "-" + PS_MAX + " mmHg",
                String.format("Presión sistólica de %d mmHg fuera de rango normal", ps)
            ));
        }

        if (pd < PD_CRITICAL_MIN || pd > PD_CRITICAL_MAX) {
            anomalies.add(new AlertMessage.Anomaly(
                "CRITICA",
                "Presión Diastólica",
                pd + " mmHg",
                PD_MIN + "-" + PD_MAX + " mmHg",
                String.format("Presión diastólica de %d mmHg está en rango crítico", pd)
            ));
        } else if (pd < PD_MIN || pd > PD_MAX) {
            anomalies.add(new AlertMessage.Anomaly(
                "ALTA",
                "Presión Diastólica",
                pd + " mmHg",
                PD_MIN + "-" + PD_MAX + " mmHg",
                String.format("Presión diastólica de %d mmHg fuera de rango normal", pd)
            ));
        }
    }

    private void checkTemperatura(BigDecimal temp, List<AlertMessage.Anomaly> anomalies) {
        if (temp.compareTo(TEMP_CRITICAL_MIN) < 0 || temp.compareTo(TEMP_CRITICAL_MAX) > 0) {
            anomalies.add(new AlertMessage.Anomaly(
                "CRITICA",
                "Temperatura",
                temp + " °C",
                TEMP_MIN + "-" + TEMP_MAX + " °C",
                String.format("Temperatura de %s °C está en rango crítico", temp)
            ));
        } else if (temp.compareTo(TEMP_MIN) < 0 || temp.compareTo(TEMP_MAX) > 0) {
            anomalies.add(new AlertMessage.Anomaly(
                "MEDIA",
                "Temperatura",
                temp + " °C",
                TEMP_MIN + "-" + TEMP_MAX + " °C",
                String.format("Temperatura de %s °C fuera de rango normal", temp)
            ));
        }
    }

    private void checkSaturacionOxigeno(Integer spo2, List<AlertMessage.Anomaly> anomalies) {
        if (spo2 < SPO2_CRITICAL) {
            anomalies.add(new AlertMessage.Anomaly(
                "CRITICA",
                "Saturación de Oxígeno",
                spo2 + " %",
                SPO2_MIN + "-100 %",
                String.format("Saturación de oxígeno de %d%% está en rango crítico", spo2)
            ));
        } else if (spo2 < SPO2_MIN) {
            anomalies.add(new AlertMessage.Anomaly(
                "ALTA",
                "Saturación de Oxígeno",
                spo2 + " %",
                SPO2_MIN + "-100 %",
                String.format("Saturación de oxígeno de %d%% fuera de rango normal", spo2)
            ));
        }
    }

    private void checkFrecuenciaRespiratoria(Integer fr, List<AlertMessage.Anomaly> anomalies) {
        if (fr < FR_CRITICAL_MIN || fr > FR_CRITICAL_MAX) {
            anomalies.add(new AlertMessage.Anomaly(
                "CRITICA",
                "Frecuencia Respiratoria",
                fr + " rpm",
                FR_MIN + "-" + FR_MAX + " rpm",
                String.format("Frecuencia respiratoria de %d rpm está en rango crítico", fr)
            ));
        } else if (fr < FR_MIN || fr > FR_MAX) {
            anomalies.add(new AlertMessage.Anomaly(
                "MEDIA",
                "Frecuencia Respiratoria",
                fr + " rpm",
                FR_MIN + "-" + FR_MAX + " rpm",
                String.format("Frecuencia respiratoria de %d rpm fuera de rango normal", fr)
            ));
        }
    }

    private AlertMessage buildAlertMessage(VitalSignsCheckRequest request, List<AlertMessage.Anomaly> anomalies) {
        AlertMessage alert = new AlertMessage();
        alert.setPacienteId(request.getPacienteId());
        alert.setPacienteNombre(request.getPacienteNombre());
        alert.setSala(request.getSala());
        alert.setCama(request.getCama());
        alert.setAnomalies(anomalies);
        alert.setSeverity(calculateSeverity(anomalies));
        alert.setDetectedAt(LocalDateTime.now());
        alert.setDeviceId(request.getDeviceId());

        AlertMessage.VitalSignsData vitalSigns = new AlertMessage.VitalSignsData();
        vitalSigns.setFrecuenciaCardiaca(request.getFrecuenciaCardiaca());
        vitalSigns.setPresionSistolica(request.getPresionSistolica());
        vitalSigns.setPresionDiastolica(request.getPresionDiastolica());
        vitalSigns.setTemperatura(request.getTemperatura().toString());
        vitalSigns.setSaturacionOxigeno(request.getSaturacionOxigeno());
        vitalSigns.setFrecuenciaRespiratoria(request.getFrecuenciaRespiratoria());
        alert.setVitalSigns(vitalSigns);

        return alert;
    }

    private String calculateSeverity(List<AlertMessage.Anomaly> anomalies) {
        boolean hasCritica = anomalies.stream()
                .anyMatch(a -> "CRITICA".equals(a.getTipo()));
        
        if (hasCritica) {
            return "CRITICA";
        }

        boolean hasAlta = anomalies.stream()
                .anyMatch(a -> "ALTA".equals(a.getTipo()));
        
        if (hasAlta) {
            return "ALTA";
        }

        return "MEDIA";
    }

    private void publishAlert(AlertMessage alert) {
        try {
            rabbitTemplate.convertAndSend(alertsQueue, alert);
            log.info("📤 Alerta publicada a RabbitMQ: Paciente {} - Severidad: {} - {} anomalías",
                    alert.getPacienteId(), alert.getSeverity(), alert.getAnomalies().size());
        } catch (Exception e) {
            log.error("❌ Error publicando alerta a RabbitMQ", e);
            throw new RuntimeException("Error al publicar alerta: " + e.getMessage());
        }
    }
}
