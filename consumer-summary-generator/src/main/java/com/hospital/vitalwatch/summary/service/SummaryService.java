package com.hospital.vitalwatch.summary.service;

import com.hospital.vitalwatch.summary.model.ResumenDiarioKafka;
import com.hospital.vitalwatch.summary.repository.ResumenDiarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SummaryService {

    private final ResumenDiarioRepository resumenRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public ResumenDiarioKafka generateDailySummary(LocalDate fecha) {
        log.info("📊 Generando resumen diario para {}", fecha);
        
        Map<String, Object> vitalSignsStats = getVitalSignsStats(fecha);
        Map<String, Object> alertsStats = getAlertsStats(fecha);
        
        ResumenDiarioKafka resumen = ResumenDiarioKafka.builder()
            .fecha(fecha)
            .totalPacientesMonitoreados(((Number) vitalSignsStats.get("total_pacientes")).longValue())
            .totalMediciones(((Number) vitalSignsStats.get("total_mediciones")).longValue())
            .medicionesPorHora(((Number) vitalSignsStats.get("mediciones_por_hora")).doubleValue())
            .promedioFrecuenciaCardiaca(getDouble(vitalSignsStats, "avg_fc"))
            .promedioPresionSistolica(getDouble(vitalSignsStats, "avg_ps"))
            .promedioPresionDiastolica(getDouble(vitalSignsStats, "avg_pd"))
            .promedioTemperatura(getDouble(vitalSignsStats, "avg_temp"))
            .promedioSaturacionOxigeno(getDouble(vitalSignsStats, "avg_spo2"))
            .promedioFrecuenciaRespiratoria(getDouble(vitalSignsStats, "avg_fr"))
            .maxFrecuenciaCardiaca(getInteger(vitalSignsStats, "max_fc"))
            .minFrecuenciaCardiaca(getInteger(vitalSignsStats, "min_fc"))
            .maxTemperatura(getDouble(vitalSignsStats, "max_temp"))
            .minTemperatura(getDouble(vitalSignsStats, "min_temp"))
            .minSaturacionOxigeno(getInteger(vitalSignsStats, "min_spo2"))
            .mensajesProcesadosVitalSigns(((Number) vitalSignsStats.get("total_mediciones")).longValue())
            .totalAlertas(((Number) alertsStats.get("total_alertas")).longValue())
            .alertasCriticas(((Number) alertsStats.get("alertas_criticas")).longValue())
            .alertasAltas(((Number) alertsStats.get("alertas_altas")).longValue())
            .alertasModeradas(((Number) alertsStats.get("alertas_moderadas")).longValue())
            .alertasBajas(((Number) alertsStats.get("alertas_bajas")).longValue())
            .pacientesConAlertas(((Number) alertsStats.get("pacientes_con_alertas")).longValue())
            .mensajesProcesadosAlertas(((Number) alertsStats.get("total_alertas")).longValue())
            .build();
        
        ResumenDiarioKafka saved = resumenRepository.findByFecha(fecha)
            .map(existing -> {
                existing.setTotalPacientesMonitoreados(resumen.getTotalPacientesMonitoreados());
                existing.setTotalMediciones(resumen.getTotalMediciones());
                existing.setMedicionesPorHora(resumen.getMedicionesPorHora());
                existing.setPromedioFrecuenciaCardiaca(resumen.getPromedioFrecuenciaCardiaca());
                existing.setPromedioPresionSistolica(resumen.getPromedioPresionSistolica());
                existing.setPromedioPresionDiastolica(resumen.getPromedioPresionDiastolica());
                existing.setPromedioTemperatura(resumen.getPromedioTemperatura());
                existing.setPromedioSaturacionOxigeno(resumen.getPromedioSaturacionOxigeno());
                existing.setPromedioFrecuenciaRespiratoria(resumen.getPromedioFrecuenciaRespiratoria());
                existing.setMaxFrecuenciaCardiaca(resumen.getMaxFrecuenciaCardiaca());
                existing.setMinFrecuenciaCardiaca(resumen.getMinFrecuenciaCardiaca());
                existing.setMaxTemperatura(resumen.getMaxTemperatura());
                existing.setMinTemperatura(resumen.getMinTemperatura());
                existing.setMinSaturacionOxigeno(resumen.getMinSaturacionOxigeno());
                existing.setMensajesProcesadosVitalSigns(resumen.getMensajesProcesadosVitalSigns());
                existing.setTotalAlertas(resumen.getTotalAlertas());
                existing.setAlertasCriticas(resumen.getAlertasCriticas());
                existing.setAlertasAltas(resumen.getAlertasAltas());
                existing.setAlertasModeradas(resumen.getAlertasModeradas());
                existing.setAlertasBajas(resumen.getAlertasBajas());
                existing.setPacientesConAlertas(resumen.getPacientesConAlertas());
                existing.setMensajesProcesadosAlertas(resumen.getMensajesProcesadosAlertas());
                return existing;
            })
            .orElse(resumen);
        
        saved = resumenRepository.save(saved);
        
        log.info("✅ Resumen diario guardado - Pacientes: {} | Mediciones: {} | Alertas: {}",
            saved.getTotalPacientesMonitoreados(),
            saved.getTotalMediciones(),
            saved.getTotalAlertas()
        );
        
        return saved;
    }

    private Map<String, Object> getVitalSignsStats(LocalDate fecha) {
        String sql = """
            SELECT 
                COUNT(DISTINCT paciente_id) as total_pacientes,
                COUNT(*) as total_mediciones,
                ROUND(COUNT(*) / 24.0, 2) as mediciones_por_hora,
                ROUND(AVG(frecuencia_cardiaca), 2) as avg_fc,
                ROUND(AVG(presion_sistolica), 2) as avg_ps,
                ROUND(AVG(presion_diastolica), 2) as avg_pd,
                ROUND(AVG(temperatura), 2) as avg_temp,
                ROUND(AVG(saturacion_oxigeno), 2) as avg_spo2,
                ROUND(AVG(frecuencia_respiratoria), 2) as avg_fr,
                MAX(frecuencia_cardiaca) as max_fc,
                MIN(frecuencia_cardiaca) as min_fc,
                MAX(temperatura) as max_temp,
                MIN(temperatura) as min_temp,
                MIN(saturacion_oxigeno) as min_spo2
            FROM SIGNOS_VITALES_KAFKA
            WHERE TRUNC(timestamp_medicion) = TO_DATE(?, 'YYYY-MM-DD')
            """;
        
        return jdbcTemplate.queryForMap(sql, fecha.toString());
    }

    private Map<String, Object> getAlertsStats(LocalDate fecha) {
        String sql = """
            SELECT 
                COUNT(*) as total_alertas,
                COUNT(DISTINCT paciente_id) as pacientes_con_alertas,
                SUM(CASE WHEN severidad = 'CRITICA' THEN 1 ELSE 0 END) as alertas_criticas,
                SUM(CASE WHEN severidad = 'ALTA' THEN 1 ELSE 0 END) as alertas_altas,
                SUM(CASE WHEN severidad = 'MODERADA' THEN 1 ELSE 0 END) as alertas_moderadas,
                SUM(CASE WHEN severidad = 'BAJA' THEN 1 ELSE 0 END) as alertas_bajas
            FROM ALERTAS_KAFKA
            WHERE TRUNC(detected_at) = TO_DATE(?, 'YYYY-MM-DD')
            """;
        
        return jdbcTemplate.queryForMap(sql, fecha.toString());
    }

    private Double getDouble(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        return ((Number) value).doubleValue();
    }

    private Integer getInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        return ((Number) value).intValue();
    }
}
