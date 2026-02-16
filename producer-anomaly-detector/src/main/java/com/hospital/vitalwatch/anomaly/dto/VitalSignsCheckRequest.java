package com.hospital.vitalwatch.anomaly.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para recibir signos vitales a verificar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VitalSignsCheckRequest {

    @NotNull(message = "El ID del paciente es obligatorio")
    private Long pacienteId;

    @NotBlank(message = "El nombre del paciente es obligatorio")
    private String pacienteNombre;

    @NotBlank(message = "La sala es obligatoria")
    private String sala;

    @NotBlank(message = "La cama es obligatoria")
    private String cama;

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

    @NotBlank(message = "El dispositivo es obligatorio")
    private String deviceId;
}
