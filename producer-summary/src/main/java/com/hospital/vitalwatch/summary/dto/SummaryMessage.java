package com.hospital.vitalwatch.summary.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Mensaje de resumen periódico que se publica a RabbitMQ
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummaryMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private LocalDateTime timestamp;
    private String summaryType;
    private Integer totalPacientes;
    private Integer pacientesCriticos;
    private Integer pacientesMonitoreados;
    private Integer alertasGeneradas;
    private Integer alertasCriticas;
    private Double promedioFrecuenciaCardiaca;
    private Double promedioTemperatura;
    private Double promedioSaturacionOxigeno;
    private List<PacienteStatus> pacientesStatus;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PacienteStatus implements Serializable {
        private Long pacienteId;
        private String pacienteNombre;
        private String sala;
        private String cama;
        private String estado;
        private Integer alertasActivas;
        private LocalDateTime ultimaMedicion;
    }
}
