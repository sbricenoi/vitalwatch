package com.hospital.vitalwatch.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad Alerta
 * Representa una alerta médica generada por el sistema o manualmente
 */
@Entity
@Table(name = "ALERTAS")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_alertas")
    @SequenceGenerator(name = "seq_alertas", sequenceName = "SEQ_ALERTAS", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @NotNull(message = "El paciente es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PACIENTE_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_ALERTAS_PACIENTE"))
    private Paciente paciente;

    @NotBlank(message = "El tipo de alerta es obligatorio")
    @Size(max = 50, message = "El tipo no puede exceder 50 caracteres")
    @Column(name = "TIPO", nullable = false, length = 50)
    private String tipo;

    @NotBlank(message = "El mensaje es obligatorio")
    @Size(max = 500, message = "El mensaje no puede exceder 500 caracteres")
    @Column(name = "MENSAJE", nullable = false, length = 500)
    private String mensaje;

    @NotBlank(message = "La severidad es obligatoria")
    @Pattern(regexp = "BAJA|MODERADA|CRÍTICA",
             message = "La severidad debe ser: BAJA, MODERADA o CRÍTICA")
    @Column(name = "SEVERIDAD", nullable = false, length = 20)
    private String severidad = "MODERADA";

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "ACTIVA|RESUELTA|DESCARTADA",
             message = "El estado debe ser: ACTIVA, RESUELTA o DESCARTADA")
    @Column(name = "ESTADO", nullable = false, length = 20)
    private String estado = "ACTIVA";

    @NotNull(message = "La fecha de creación es obligatoria")
    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "FECHA_RESOLUCION")
    private LocalDateTime fechaResolucion;

    @Size(max = 100, message = "El nombre de quien resolvió no puede exceder 100 caracteres")
    @Column(name = "RESUELTO_POR", length = 100)
    private String resueltoPor;

    @Size(max = 500, message = "Las notas de resolución no pueden exceder 500 caracteres")
    @Column(name = "NOTAS_RESOLUCION", length = 500)
    private String notasResolucion;

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    /**
     * Constructor para crear alerta sin ID
     */
    public Alerta(Paciente paciente, String tipo, String mensaje, String severidad) {
        this.paciente = paciente;
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.severidad = severidad;
        this.estado = "ACTIVA";
        this.fechaCreacion = LocalDateTime.now();
    }

    /**
     * Constructor completo sin ID
     */
    public Alerta(Paciente paciente, String tipo, String mensaje, String severidad, 
                 String estado, LocalDateTime fechaCreacion) {
        this.paciente = paciente;
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.severidad = severidad;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = "ACTIVA";
        }
        if (severidad == null) {
            severidad = "MODERADA";
        }
    }

    /**
     * Método para resolver la alerta
     */
    public void resolver(String resueltoPor, String notasResolucion) {
        this.estado = "RESUELTA";
        this.fechaResolucion = LocalDateTime.now();
        this.resueltoPor = resueltoPor;
        this.notasResolucion = notasResolucion;
    }

    /**
     * Método para descartar la alerta
     */
    public void descartar(String descartadoPor, String motivo) {
        this.estado = "DESCARTADA";
        this.fechaResolucion = LocalDateTime.now();
        this.resueltoPor = descartadoPor;
        this.notasResolucion = "DESCARTADA: " + motivo;
    }

    /**
     * Verifica si la alerta está activa
     */
    public boolean estaActiva() {
        return "ACTIVA".equals(this.estado);
    }

    /**
     * Verifica si la alerta es crítica
     */
    public boolean esCritica() {
        return "CRÍTICA".equals(this.severidad);
    }

    /**
     * Calcula el tiempo transcurrido desde la creación (en minutos)
     */
    public long tiempoTranscurridoMinutos() {
        return java.time.Duration.between(fechaCreacion, LocalDateTime.now()).toMinutes();
    }
}
