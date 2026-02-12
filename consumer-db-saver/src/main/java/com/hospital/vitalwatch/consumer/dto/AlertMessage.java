package com.hospital.vitalwatch.consumer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO que representa el mensaje de alerta recibido de RabbitMQ
 * Debe coincidir con el formato enviado por el Productor
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private Long pacienteId;
    private String pacienteNombre;
    private String sala;
    private String cama;
    private List<Anomaly> anomalies;
    private String severity;
    private LocalDateTime detectedAt;
    private String deviceId;
    private VitalSignsData vitalSigns;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Anomaly implements Serializable {
        private String tipo;
        private String parametro;
        private String valorActual;
        private String rangoNormal;
        private String mensaje;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VitalSignsData implements Serializable {
        private Integer frecuenciaCardiaca;
        private Integer presionSistolica;
        private Integer presionDiastolica;
        private String temperatura;
        private Integer saturacionOxigeno;
        private Integer frecuenciaRespiratoria;
    }
}
