package com.hospital.vitalwatch.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para SignosVitales
 * Usado para transferir datos de signos vitales en requests y responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignosVitalesDTO {

    private Long id;

    @NotNull(message = "El ID del paciente es obligatorio")
    private Long pacienteId;

    // Información del paciente (solo en responses)
    private String pacienteNombre;
    private String pacienteSala;
    private String pacienteCama;

    @NotNull(message = "La frecuencia cardíaca es obligatoria")
    @Min(value = 0, message = "La frecuencia cardíaca debe ser mayor o igual a 0")
    @Max(value = 300, message = "La frecuencia cardíaca debe ser menor o igual a 300")
    private Integer frecuenciaCardiaca;

    @NotNull(message = "La presión sistólica es obligatoria")
    @Min(value = 0, message = "La presión sistólica debe ser mayor o igual a 0")
    @Max(value = 300, message = "La presión sistólica debe ser menor o igual a 300")
    private Integer presionSistolica;

    @NotNull(message = "La presión diastólica es obligatoria")
    @Min(value = 0, message = "La presión diastólica debe ser mayor o igual a 0")
    @Max(value = 200, message = "La presión diastólica debe ser menor o igual a 200")
    private Integer presionDiastolica;

    @NotNull(message = "La temperatura es obligatoria")
    @DecimalMin(value = "30.0", message = "La temperatura debe ser mayor o igual a 30.0")
    @DecimalMax(value = "45.0", message = "La temperatura debe ser menor o igual a 45.0")
    private BigDecimal temperatura;

    @NotNull(message = "La saturación de oxígeno es obligatoria")
    @Min(value = 0, message = "La saturación debe ser mayor o igual a 0")
    @Max(value = 100, message = "La saturación debe ser menor o igual a 100")
    private Integer saturacionOxigeno;

    @NotNull(message = "La frecuencia respiratoria es obligatoria")
    @Min(value = 0, message = "La frecuencia respiratoria debe ser mayor o igual a 0")
    @Max(value = 100, message = "La frecuencia respiratoria debe ser menor o igual a 100")
    private Integer frecuenciaRespiratoria;

    @NotBlank(message = "El estado de conciencia es obligatorio")
    @Pattern(regexp = "ALERTA|VERBAL|DOLOR|INCONSCIENTE",
            message = "El estado de conciencia debe ser: ALERTA, VERBAL, DOLOR o INCONSCIENTE")
    private String estadoConciencia;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;

    private LocalDateTime fechaRegistro;

    @NotBlank(message = "El registrador es obligatorio")
    @Size(max = 100, message = "El nombre del registrador no puede exceder 100 caracteres")
    private String registradoPor;

    // Campos calculados (solo en responses)
    private Boolean tieneAlgunaAnormalidad;
    private Boolean esCritico;

    /**
     * Constructor para requests (sin campos calculados ni info del paciente)
     */
    public SignosVitalesDTO(Long pacienteId, Integer frecuenciaCardiaca, Integer presionSistolica,
                           Integer presionDiastolica, BigDecimal temperatura, Integer saturacionOxigeno,
                           Integer frecuenciaRespiratoria, String estadoConciencia,
                           String observaciones, String registradoPor) {
        this.pacienteId = pacienteId;
        this.frecuenciaCardiaca = frecuenciaCardiaca;
        this.presionSistolica = presionSistolica;
        this.presionDiastolica = presionDiastolica;
        this.temperatura = temperatura;
        this.saturacionOxigeno = saturacionOxigeno;
        this.frecuenciaRespiratoria = frecuenciaRespiratoria;
        this.estadoConciencia = estadoConciencia;
        this.observaciones = observaciones;
        this.registradoPor = registradoPor;
        this.fechaRegistro = LocalDateTime.now();
    }
}
