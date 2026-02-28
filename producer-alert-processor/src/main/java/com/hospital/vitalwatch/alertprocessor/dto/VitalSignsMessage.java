package com.hospital.vitalwatch.alertprocessor.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VitalSignsMessage {
    private String messageId;
    private String pacienteId;
    private String pacienteNombre;
    private String sala;
    private String cama;
    private Integer frecuenciaCardiaca;
    private Integer presionSistolica;
    private Integer presionDiastolica;
    private Double temperatura;
    private Integer saturacionOxigeno;
    private Integer frecuenciaRespiratoria;
    private String deviceId;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime timestamp;
    
    private String source;
}
