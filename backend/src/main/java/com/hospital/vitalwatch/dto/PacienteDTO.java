package com.hospital.vitalwatch.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para Paciente
 * Usado para transferir datos de pacientes en requests y responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PacienteDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    private String apellido;

    @NotBlank(message = "El RUT es obligatorio")
    @Pattern(regexp = "^\\d{7,8}-[\\dkK]$", message = "Formato de RUT inválido (ej: 12345678-9)")
    private String rut;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    private LocalDate fechaNacimiento;

    @NotNull(message = "La edad es obligatoria")
    @Min(value = 0, message = "La edad debe ser mayor o igual a 0")
    @Max(value = 120, message = "La edad debe ser menor o igual a 120")
    private Integer edad;

    @NotNull(message = "El género es obligatorio")
    @Pattern(regexp = "[MFO]", message = "El género debe ser M, F u O")
    private String genero;

    @NotBlank(message = "La sala es obligatoria")
    @Size(max = 20, message = "La sala no puede exceder 20 caracteres")
    private String sala;

    @NotBlank(message = "La cama es obligatoria")
    @Size(max = 10, message = "La cama no puede exceder 10 caracteres")
    private String cama;

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "ESTABLE|MODERADO|CRÍTICO|RECUPERACIÓN",
            message = "El estado debe ser: ESTABLE, MODERADO, CRÍTICO o RECUPERACIÓN")
    private String estado;

    @Size(max = 500, message = "El diagnóstico no puede exceder 500 caracteres")
    private String diagnostico;

    private LocalDateTime fechaIngreso;

    private LocalDateTime fechaAlta;

    // Campos calculados (no enviados en requests, solo en responses)
    private String nombreCompleto;
    private Boolean esCritico;

    /**
     * Constructor sin campos calculados (para requests)
     */
    public PacienteDTO(Long id, String nombre, String apellido, String rut,
                      LocalDate fechaNacimiento, Integer edad, String genero,
                      String sala, String cama, String estado, String diagnostico,
                      LocalDateTime fechaIngreso, LocalDateTime fechaAlta) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.rut = rut;
        this.fechaNacimiento = fechaNacimiento;
        this.edad = edad;
        this.genero = genero;
        this.sala = sala;
        this.cama = cama;
        this.estado = estado;
        this.diagnostico = diagnostico;
        this.fechaIngreso = fechaIngreso;
        this.fechaAlta = fechaAlta;
        this.nombreCompleto = nombre + " " + apellido;
        this.esCritico = "CRÍTICO".equals(estado);
    }
}
