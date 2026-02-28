package com.hospital.vitalwatch.alertprocessor.service;

import com.hospital.vitalwatch.alertprocessor.dto.AlertMessage;
import com.hospital.vitalwatch.alertprocessor.dto.VitalSignsMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class AnomalyDetectionService {

    public AlertMessage detectAnomalies(VitalSignsMessage vitalSigns) {
        List<AlertMessage.Anomaly> anomalies = new ArrayList<>();
        
        checkFrecuenciaCardiaca(vitalSigns.getFrecuenciaCardiaca(), anomalies);
        checkPresionArterial(vitalSigns.getPresionSistolica(), vitalSigns.getPresionDiastolica(), anomalies);
        checkTemperatura(vitalSigns.getTemperatura(), anomalies);
        checkSaturacionOxigeno(vitalSigns.getSaturacionOxigeno(), anomalies);
        checkFrecuenciaRespiratoria(vitalSigns.getFrecuenciaRespiratoria(), anomalies);
        
        if (anomalies.isEmpty()) {
            return null;
        }
        
        String severity = calculateSeverity(anomalies);
        String mensaje = buildAlertMessage(anomalies, vitalSigns.getPacienteNombre());
        
        return AlertMessage.builder()
            .alertId("ALERT-" + UUID.randomUUID().toString())
            .pacienteId(vitalSigns.getPacienteId())
            .pacienteNombre(vitalSigns.getPacienteNombre())
            .sala(vitalSigns.getSala())
            .cama(vitalSigns.getCama())
            .tipoAlerta("SIGNOS_VITALES_ANORMALES")
            .mensaje(mensaje)
            .severidad(severity)
            .frecuenciaCardiaca(vitalSigns.getFrecuenciaCardiaca())
            .presionSistolica(vitalSigns.getPresionSistolica())
            .presionDiastolica(vitalSigns.getPresionDiastolica())
            .temperatura(vitalSigns.getTemperatura())
            .saturacionOxigeno(vitalSigns.getSaturacionOxigeno())
            .frecuenciaRespiratoria(vitalSigns.getFrecuenciaRespiratoria())
            .anomalias(anomalies)
            .cantidadAnomalias(anomalies.size())
            .deviceId(vitalSigns.getDeviceId())
            .detectedAt(LocalDateTime.now())
            .source("ALERT_PROCESSOR")
            .build();
    }

    private void checkFrecuenciaCardiaca(Integer fc, List<AlertMessage.Anomaly> anomalies) {
        if (fc == null) return;
        
        if (fc < 40) {
            anomalies.add(createAnomaly("CRITICA", "Frecuencia Cardíaca", fc + " lpm", "60-100 lpm", "Bradicardia severa"));
        } else if (fc < 60) {
            anomalies.add(createAnomaly("MODERADA", "Frecuencia Cardíaca", fc + " lpm", "60-100 lpm", "Bradicardia"));
        } else if (fc > 120) {
            anomalies.add(createAnomaly("CRITICA", "Frecuencia Cardíaca", fc + " lpm", "60-100 lpm", "Taquicardia severa"));
        } else if (fc > 100) {
            anomalies.add(createAnomaly("MODERADA", "Frecuencia Cardíaca", fc + " lpm", "60-100 lpm", "Taquicardia"));
        }
    }

    private void checkPresionArterial(Integer sistolica, Integer diastolica, List<AlertMessage.Anomaly> anomalies) {
        if (sistolica != null) {
            if (sistolica < 70) {
                anomalies.add(createAnomaly("CRITICA", "Presión Sistólica", sistolica + " mmHg", "90-120 mmHg", "Hipotensión severa"));
            } else if (sistolica < 90) {
                anomalies.add(createAnomaly("MODERADA", "Presión Sistólica", sistolica + " mmHg", "90-120 mmHg", "Hipotensión"));
            } else if (sistolica > 160) {
                anomalies.add(createAnomaly("CRITICA", "Presión Sistólica", sistolica + " mmHg", "90-120 mmHg", "Hipertensión severa"));
            } else if (sistolica > 140) {
                anomalies.add(createAnomaly("ALTA", "Presión Sistólica", sistolica + " mmHg", "90-120 mmHg", "Hipertensión"));
            }
        }
        
        if (diastolica != null) {
            if (diastolica < 40) {
                anomalies.add(createAnomaly("CRITICA", "Presión Diastólica", diastolica + " mmHg", "60-80 mmHg", "Hipotensión severa"));
            } else if (diastolica < 60) {
                anomalies.add(createAnomaly("MODERADA", "Presión Diastólica", diastolica + " mmHg", "60-80 mmHg", "Hipotensión"));
            } else if (diastolica > 100) {
                anomalies.add(createAnomaly("CRITICA", "Presión Diastólica", diastolica + " mmHg", "60-80 mmHg", "Hipertensión severa"));
            } else if (diastolica > 90) {
                anomalies.add(createAnomaly("ALTA", "Presión Diastólica", diastolica + " mmHg", "60-80 mmHg", "Hipertensión"));
            }
        }
    }

    private void checkTemperatura(Double temp, List<AlertMessage.Anomaly> anomalies) {
        if (temp == null) return;
        
        if (temp < 35.0) {
            anomalies.add(createAnomaly("CRITICA", "Temperatura", String.format("%.1f °C", temp), "36.0-37.5 °C", "Hipotermia severa"));
        } else if (temp < 36.0) {
            anomalies.add(createAnomaly("MODERADA", "Temperatura", String.format("%.1f °C", temp), "36.0-37.5 °C", "Hipotermia"));
        } else if (temp > 39.5) {
            anomalies.add(createAnomaly("CRITICA", "Temperatura", String.format("%.1f °C", temp), "36.0-37.5 °C", "Fiebre alta"));
        } else if (temp > 38.0) {
            anomalies.add(createAnomaly("MODERADA", "Temperatura", String.format("%.1f °C", temp), "36.0-37.5 °C", "Fiebre"));
        }
    }

    private void checkSaturacionOxigeno(Integer spo2, List<AlertMessage.Anomaly> anomalies) {
        if (spo2 == null) return;
        
        if (spo2 < 90) {
            anomalies.add(createAnomaly("CRITICA", "Saturación O2", spo2 + " %", "95-100 %", "Hipoxemia crítica"));
        } else if (spo2 < 95) {
            anomalies.add(createAnomaly("MODERADA", "Saturación O2", spo2 + " %", "95-100 %", "Saturación baja"));
        }
    }

    private void checkFrecuenciaRespiratoria(Integer fr, List<AlertMessage.Anomaly> anomalies) {
        if (fr == null) return;
        
        if (fr < 8) {
            anomalies.add(createAnomaly("CRITICA", "Frecuencia Respiratoria", fr + " rpm", "12-20 rpm", "Bradipnea severa"));
        } else if (fr < 12) {
            anomalies.add(createAnomaly("MODERADA", "Frecuencia Respiratoria", fr + " rpm", "12-20 rpm", "Bradipnea"));
        } else if (fr > 25) {
            anomalies.add(createAnomaly("CRITICA", "Frecuencia Respiratoria", fr + " rpm", "12-20 rpm", "Taquipnea severa"));
        } else if (fr > 20) {
            anomalies.add(createAnomaly("MODERADA", "Frecuencia Respiratoria", fr + " rpm", "12-20 rpm", "Taquipnea"));
        }
    }

    private AlertMessage.Anomaly createAnomaly(String tipo, String parametro, String valorActual, String rangoNormal, String descripcion) {
        return AlertMessage.Anomaly.builder()
            .tipo(tipo)
            .parametro(parametro)
            .valorActual(valorActual)
            .rangoNormal(rangoNormal)
            .build();
    }

    private String calculateSeverity(List<AlertMessage.Anomaly> anomalies) {
        boolean hasCritica = anomalies.stream().anyMatch(a -> "CRITICA".equals(a.getTipo()));
        boolean hasAlta = anomalies.stream().anyMatch(a -> "ALTA".equals(a.getTipo()));
        boolean hasModerada = anomalies.stream().anyMatch(a -> "MODERADA".equals(a.getTipo()));
        
        if (hasCritica) return "CRITICA";
        if (hasAlta) return "ALTA";
        if (hasModerada) return "MODERADA";
        return "BAJA";
    }

    private String buildAlertMessage(List<AlertMessage.Anomaly> anomalies, String pacienteNombre) {
        StringBuilder sb = new StringBuilder();
        sb.append("ALERTA MÉDICA: Se detectaron ").append(anomalies.size())
          .append(" anomalías en los signos vitales de ").append(pacienteNombre).append(". ");
        
        long criticas = anomalies.stream().filter(a -> "CRITICA".equals(a.getTipo())).count();
        if (criticas > 0) {
            sb.append("ATENCIÓN INMEDIATA REQUERIDA. ");
        }
        
        sb.append("Parámetros afectados: ");
        anomalies.forEach(a -> sb.append(a.getParametro()).append(" (").append(a.getValorActual()).append("), "));
        
        return sb.toString();
    }
}
