package com.hospital.vitalwatch.alertprocessor.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertMessage {
    
    private String alertId;
    
    private String pacienteId;
    private String pacienteNombre;
    private String sala;
    private String cama;
    
    private String tipoAlerta;
    private String mensaje;
    private String severidad;
    
    private Integer frecuenciaCardiaca;
    private Integer presionSistolica;
    private Integer presionDiastolica;
    private Double temperatura;
    private Integer saturacionOxigeno;
    private Integer frecuenciaRespiratoria;
    
    private List<Anomaly> anomalias;
    private Integer cantidadAnomalias;
    
    private String deviceId;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime detectedAt;
    
    private String source;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Anomaly {
        private String tipo;
        private String parametro;
        private String valorActual;
        private String rangoNormal;
    }
}
