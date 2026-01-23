package com.hospital.vitalwatch.service;

import com.hospital.vitalwatch.model.Alerta;
import com.hospital.vitalwatch.model.SignosVitales;
import com.hospital.vitalwatch.repository.AlertaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de Monitoreo
 * Responsable de evaluar signos vitales y generar alertas automáticamente
 * cuando los valores están fuera de rangos seguros
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoreoService {

    private final AlertaRepository alertaRepository;

    /**
     * MÉTODO PRINCIPAL: Evalúa signos vitales y genera alertas si es necesario
     * Este método es llamado automáticamente después de registrar signos vitales
     */
    @Transactional
    public void evaluarYGenerarAlertas(SignosVitales signosVitales) {
        log.info("Evaluando signos vitales ID: {} del paciente: {}", 
                 signosVitales.getId(), signosVitales.getPaciente().getNombreCompleto());

        List<Alerta> alertasGeneradas = new ArrayList<>();

        // Evaluar cada signo vital
        alertasGeneradas.addAll(evaluarFrecuenciaCardiaca(signosVitales));
        alertasGeneradas.addAll(evaluarPresionArterial(signosVitales));
        alertasGeneradas.addAll(evaluarTemperatura(signosVitales));
        alertasGeneradas.addAll(evaluarSaturacionOxigeno(signosVitales));
        alertasGeneradas.addAll(evaluarFrecuenciaRespiratoria(signosVitales));

        // Guardar todas las alertas generadas
        if (!alertasGeneradas.isEmpty()) {
            alertaRepository.saveAll(alertasGeneradas);
            log.warn("Se generaron {} alertas automáticas para el paciente {}", 
                     alertasGeneradas.size(), 
                     signosVitales.getPaciente().getNombreCompleto());
        } else {
            log.info("Signos vitales dentro de rangos normales. No se generaron alertas.");
        }
    }

    /**
     * Evalúa la frecuencia cardíaca
     * Rango normal: 60-100 bpm
     * Alerta moderada: 100-120 o 50-60 bpm
     * Alerta crítica: >120 o <50 bpm
     */
    private List<Alerta> evaluarFrecuenciaCardiaca(SignosVitales signos) {
        List<Alerta> alertas = new ArrayList<>();
        Integer fc = signos.getFrecuenciaCardiaca();

        if (fc > 120) {
            alertas.add(crearAlerta(
                signos,
                "FRECUENCIA_CARDIACA_ALTA",
                String.format("Frecuencia cardíaca de %d bpm excede el límite crítico (>120 bpm). " +
                             "Taquicardia severa. Evaluación inmediata requerida.", fc),
                "CRÍTICA"
            ));
        } else if (fc >= 100) {
            alertas.add(crearAlerta(
                signos,
                "FRECUENCIA_CARDIACA_ELEVADA",
                String.format("Frecuencia cardíaca de %d bpm por encima del rango normal (100-120 bpm). " +
                             "Monitoreo continuo recomendado.", fc),
                "MODERADA"
            ));
        } else if (fc < 50) {
            alertas.add(crearAlerta(
                signos,
                "FRECUENCIA_CARDIACA_BAJA",
                String.format("Frecuencia cardíaca de %d bpm por debajo del límite crítico (<50 bpm). " +
                             "Bradicardia severa. Intervención inmediata requerida.", fc),
                "CRÍTICA"
            ));
        } else if (fc < 60) {
            alertas.add(crearAlerta(
                signos,
                "FRECUENCIA_CARDIACA_REDUCIDA",
                String.format("Frecuencia cardíaca de %d bpm por debajo del rango normal (50-60 bpm). " +
                             "Monitoreo recomendado.", fc),
                "MODERADA"
            ));
        }

        return alertas;
    }

    /**
     * Evalúa la presión arterial
     * Rango normal sistólica: 90-140 mmHg
     * Rango normal diastólica: 60-90 mmHg
     */
    private List<Alerta> evaluarPresionArterial(SignosVitales signos) {
        List<Alerta> alertas = new ArrayList<>();
        Integer sistolica = signos.getPresionSistolica();
        Integer diastolica = signos.getPresionDiastolica();

        // Evaluar presión sistólica
        if (sistolica > 160) {
            alertas.add(crearAlerta(
                signos,
                "PRESION_SISTOLICA_ALTA",
                String.format("Presión sistólica de %d mmHg excede el límite crítico (>160 mmHg). " +
                             "Crisis hipertensiva. Intervención urgente requerida.", sistolica),
                "CRÍTICA"
            ));
        } else if (sistolica > 140) {
            alertas.add(crearAlerta(
                signos,
                "PRESION_SISTOLICA_ELEVADA",
                String.format("Presión sistólica de %d mmHg por encima del rango normal (140-160 mmHg). " +
                             "Hipertensión. Monitoreo recomendado.", sistolica),
                "MODERADA"
            ));
        } else if (sistolica < 80) {
            alertas.add(crearAlerta(
                signos,
                "PRESION_SISTOLICA_BAJA",
                String.format("Presión sistólica de %d mmHg por debajo del límite crítico (<80 mmHg). " +
                             "Hipotensión severa. Evaluación inmediata requerida.", sistolica),
                "CRÍTICA"
            ));
        } else if (sistolica < 90) {
            alertas.add(crearAlerta(
                signos,
                "PRESION_SISTOLICA_REDUCIDA",
                String.format("Presión sistólica de %d mmHg por debajo del rango normal (80-90 mmHg). " +
                             "Monitoreo recomendado.", sistolica),
                "MODERADA"
            ));
        }

        // Evaluar presión diastólica
        if (diastolica > 100) {
            alertas.add(crearAlerta(
                signos,
                "PRESION_DIASTOLICA_ALTA",
                String.format("Presión diastólica de %d mmHg excede el límite crítico (>100 mmHg). " +
                             "Crisis hipertensiva. Intervención urgente requerida.", diastolica),
                "CRÍTICA"
            ));
        } else if (diastolica > 90) {
            alertas.add(crearAlerta(
                signos,
                "PRESION_DIASTOLICA_ELEVADA",
                String.format("Presión diastólica de %d mmHg por encima del rango normal (90-100 mmHg). " +
                             "Monitoreo recomendado.", diastolica),
                "MODERADA"
            ));
        } else if (diastolica < 50) {
            alertas.add(crearAlerta(
                signos,
                "PRESION_DIASTOLICA_BAJA",
                String.format("Presión diastólica de %d mmHg por debajo del límite crítico (<50 mmHg). " +
                             "Evaluación inmediata requerida.", diastolica),
                "CRÍTICA"
            ));
        } else if (diastolica < 60) {
            alertas.add(crearAlerta(
                signos,
                "PRESION_DIASTOLICA_REDUCIDA",
                String.format("Presión diastólica de %d mmHg por debajo del rango normal (50-60 mmHg). " +
                             "Monitoreo recomendado.", diastolica),
                "MODERADA"
            ));
        }

        return alertas;
    }

    /**
     * Evalúa la temperatura corporal
     * Rango normal: 36.0-37.5 °C
     * Alerta moderada: 37.5-38.5 o 35.0-36.0 °C
     * Alerta crítica: >38.5 o <35.0 °C
     */
    private List<Alerta> evaluarTemperatura(SignosVitales signos) {
        List<Alerta> alertas = new ArrayList<>();
        BigDecimal temp = signos.getTemperatura();

        if (temp.compareTo(new BigDecimal("38.5")) > 0) {
            alertas.add(crearAlerta(
                signos,
                "TEMPERATURA_ALTA",
                String.format("Temperatura de %.1f°C excede el límite crítico (>38.5°C). " +
                             "Fiebre alta. Evaluación inmediata y antipirético requerido.", temp),
                "CRÍTICA"
            ));
        } else if (temp.compareTo(new BigDecimal("37.5")) > 0) {
            alertas.add(crearAlerta(
                signos,
                "TEMPERATURA_ELEVADA",
                String.format("Temperatura de %.1f°C por encima del rango normal (37.5-38.5°C). " +
                             "Fiebre moderada. Monitoreo recomendado.", temp),
                "MODERADA"
            ));
        } else if (temp.compareTo(new BigDecimal("35.0")) < 0) {
            alertas.add(crearAlerta(
                signos,
                "TEMPERATURA_BAJA",
                String.format("Temperatura de %.1f°C por debajo del límite crítico (<35.0°C). " +
                             "Hipotermia. Intervención inmediata requerida.", temp),
                "CRÍTICA"
            ));
        } else if (temp.compareTo(new BigDecimal("36.0")) < 0) {
            alertas.add(crearAlerta(
                signos,
                "TEMPERATURA_REDUCIDA",
                String.format("Temperatura de %.1f°C por debajo del rango normal (35.0-36.0°C). " +
                             "Monitoreo recomendado.", temp),
                "MODERADA"
            ));
        }

        return alertas;
    }

    /**
     * Evalúa la saturación de oxígeno
     * Rango normal: 95-100%
     * Alerta moderada: 90-95%
     * Alerta crítica: <90%
     */
    private List<Alerta> evaluarSaturacionOxigeno(SignosVitales signos) {
        List<Alerta> alertas = new ArrayList<>();
        Integer spo2 = signos.getSaturacionOxigeno();

        if (spo2 < 90) {
            alertas.add(crearAlerta(
                signos,
                "SATURACION_OXIGENO_CRITICA",
                String.format("Saturación de oxígeno de %d%% por debajo del límite crítico (<90%%). " +
                             "Hipoxemia severa. Oxigenoterapia y evaluación inmediata requerida.", spo2),
                "CRÍTICA"
            ));
        } else if (spo2 < 95) {
            alertas.add(crearAlerta(
                signos,
                "SATURACION_OXIGENO_BAJA",
                String.format("Saturación de oxígeno de %d%% por debajo del rango normal (90-95%%). " +
                             "Desaturación. Monitoreo continuo y evaluación recomendada.", spo2),
                "MODERADA"
            ));
        }

        return alertas;
    }

    /**
     * Evalúa la frecuencia respiratoria
     * Rango normal: 12-20 rpm
     * Alerta moderada: 20-25 o 10-12 rpm
     * Alerta crítica: >25 o <10 rpm
     */
    private List<Alerta> evaluarFrecuenciaRespiratoria(SignosVitales signos) {
        List<Alerta> alertas = new ArrayList<>();
        Integer fr = signos.getFrecuenciaRespiratoria();

        if (fr > 25) {
            alertas.add(crearAlerta(
                signos,
                "FRECUENCIA_RESPIRATORIA_ALTA",
                String.format("Frecuencia respiratoria de %d rpm excede el límite crítico (>25 rpm). " +
                             "Taquipnea severa. Evaluación inmediata requerida.", fr),
                "CRÍTICA"
            ));
        } else if (fr > 20) {
            alertas.add(crearAlerta(
                signos,
                "FRECUENCIA_RESPIRATORIA_ELEVADA",
                String.format("Frecuencia respiratoria de %d rpm por encima del rango normal (20-25 rpm). " +
                             "Taquipnea moderada. Monitoreo recomendado.", fr),
                "MODERADA"
            ));
        } else if (fr < 10) {
            alertas.add(crearAlerta(
                signos,
                "FRECUENCIA_RESPIRATORIA_BAJA",
                String.format("Frecuencia respiratoria de %d rpm por debajo del límite crítico (<10 rpm). " +
                             "Bradipnea severa. Intervención inmediata requerida.", fr),
                "CRÍTICA"
            ));
        } else if (fr < 12) {
            alertas.add(crearAlerta(
                signos,
                "FRECUENCIA_RESPIRATORIA_REDUCIDA",
                String.format("Frecuencia respiratoria de %d rpm por debajo del rango normal (10-12 rpm). " +
                             "Monitoreo recomendado.", fr),
                "MODERADA"
            ));
        }

        return alertas;
    }

    /**
     * Método helper para crear una alerta
     */
    private Alerta crearAlerta(SignosVitales signos, String tipo, String mensaje, String severidad) {
        Alerta alerta = new Alerta();
        alerta.setPaciente(signos.getPaciente());
        alerta.setTipo(tipo);
        alerta.setMensaje(mensaje);
        alerta.setSeveridad(severidad);
        alerta.setEstado("ACTIVA");
        alerta.setFechaCreacion(LocalDateTime.now());
        
        log.info("Alerta generada: {} - {} - Severidad: {}", 
                 tipo, signos.getPaciente().getNombreCompleto(), severidad);
        
        return alerta;
    }
}
