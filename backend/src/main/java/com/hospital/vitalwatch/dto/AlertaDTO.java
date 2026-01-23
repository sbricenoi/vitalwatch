package com.hospital.vitalwatch.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para Alerta
 * Usado para transferir datos de alertas en requests y responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertaDTO {

    private Long id;

    @NotNull(message = "El ID del paciente es obligatorio")
    private Long pacienteId;

    // Información del paciente (solo en responses)
    private String pacienteNombre;
    private String pacienteSala;
    private String pacienteCama;
    private String pacienteEstado;

    @NotBlank(message = "El tipo de alerta es obligatorio")
    @Size(max = 50, message = "El tipo no puede exceder 50 caracteres")
    private String tipo;

    @NotBlank(message = "El mensaje es obligatorio")
    @Size(max = 500, message = "El mensaje no puede exceder 500 caracteres")
    private String mensaje;

    @NotBlank(message = "La severidad es obligatoria")
    @Pattern(regexp = "BAJA|MODERADA|CRÍTICA",
            message = "La severidad debe ser: BAJA, MODERADA o CRÍTICA")
    private String severidad;

    private String estado;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaResolucion;

    @Size(max = 100, message = "El nombre de quien resolvió no puede exceder 100 caracteres")
    private String resueltoPor;

    @Size(max = 500, message = "Las notas de resolución no pueden exceder 500 caracteres")
    private String notasResolucion;

    // Campos calculados (solo en responses)
    private Boolean estaActiva;
    private Boolean esCritica;
    private Long tiempoTranscurridoMinutos;

    /**
     * Constructor para crear alerta (request)
     */
    public AlertaDTO(Long pacienteId, String tipo, String mensaje, String severidad) {
        this.pacienteId = pacienteId;
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.severidad = severidad;
        this.estado = "ACTIVA";
        this.fechaCreacion = LocalDateTime.now();
    }

    /**
     * Constructor completo para responses
     */
    public AlertaDTO(Long id, Long pacienteId, String pacienteNombre, String pacienteSala,
                    String pacienteCama, String pacienteEstado, String tipo, String mensaje,
                    String severidad, String estado, LocalDateTime fechaCreacion,
                    LocalDateTime fechaResolucion, String resueltoPor, String notasResolucion) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.pacienteNombre = pacienteNombre;
        this.pacienteSala = pacienteSala;
        this.pacienteCama = pacienteCama;
        this.pacienteEstado = pacienteEstado;
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.severidad = severidad;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.fechaResolucion = fechaResolucion;
        this.resueltoPor = resueltoPor;
        this.notasResolucion = notasResolucion;
        this.estaActiva = "ACTIVA".equals(estado);
        this.esCritica = "CRÍTICA".equals(severidad);
        if (fechaCreacion != null) {
            this.tiempoTranscurridoMinutos = java.time.Duration.between(
                fechaCreacion, 
                LocalDateTime.now()
            ).toMinutes();
        }
    }
}
