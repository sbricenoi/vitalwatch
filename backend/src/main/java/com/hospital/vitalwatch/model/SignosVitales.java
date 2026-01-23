package com.hospital.vitalwatch.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad SignosVitales
 * Representa el registro de signos vitales de un paciente
 */
@Entity
@Table(name = "SIGNOS_VITALES")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignosVitales {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_signos_vitales")
    @SequenceGenerator(name = "seq_signos_vitales", sequenceName = "SEQ_SIGNOS_VITALES", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @NotNull(message = "El paciente es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PACIENTE_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_SIGNOS_PACIENTE"))
    private Paciente paciente;

    @NotNull(message = "La frecuencia cardíaca es obligatoria")
    @Min(value = 0, message = "La frecuencia cardíaca debe ser mayor o igual a 0")
    @Max(value = 300, message = "La frecuencia cardíaca debe ser menor o igual a 300")
    @Column(name = "FRECUENCIA_CARDIACA", nullable = false)
    private Integer frecuenciaCardiaca;

    @NotNull(message = "La presión sistólica es obligatoria")
    @Min(value = 0, message = "La presión sistólica debe ser mayor o igual a 0")
    @Max(value = 300, message = "La presión sistólica debe ser menor o igual a 300")
    @Column(name = "PRESION_SISTOLICA", nullable = false)
    private Integer presionSistolica;

    @NotNull(message = "La presión diastólica es obligatoria")
    @Min(value = 0, message = "La presión diastólica debe ser mayor o igual a 0")
    @Max(value = 200, message = "La presión diastólica debe ser menor o igual a 200")
    @Column(name = "PRESION_DIASTOLICA", nullable = false)
    private Integer presionDiastolica;

    @NotNull(message = "La temperatura es obligatoria")
    @DecimalMin(value = "30.0", message = "La temperatura debe ser mayor o igual a 30.0")
    @DecimalMax(value = "45.0", message = "La temperatura debe ser menor o igual a 45.0")
    @Column(name = "TEMPERATURA", nullable = false, precision = 4, scale = 2)
    private BigDecimal temperatura;

    @NotNull(message = "La saturación de oxígeno es obligatoria")
    @Min(value = 0, message = "La saturación debe ser mayor o igual a 0")
    @Max(value = 100, message = "La saturación debe ser menor o igual a 100")
    @Column(name = "SATURACION_OXIGENO", nullable = false)
    private Integer saturacionOxigeno;

    @NotNull(message = "La frecuencia respiratoria es obligatoria")
    @Min(value = 0, message = "La frecuencia respiratoria debe ser mayor o igual a 0")
    @Max(value = 100, message = "La frecuencia respiratoria debe ser menor o igual a 100")
    @Column(name = "FRECUENCIA_RESPIRATORIA", nullable = false)
    private Integer frecuenciaRespiratoria;

    @NotBlank(message = "El estado de conciencia es obligatorio")
    @Pattern(regexp = "ALERTA|VERBAL|DOLOR|INCONSCIENTE",
             message = "El estado de conciencia debe ser: ALERTA, VERBAL, DOLOR o INCONSCIENTE")
    @Column(name = "ESTADO_CONCIENCIA", nullable = false, length = 50)
    private String estadoConciencia = "ALERTA";

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    @Column(name = "OBSERVACIONES", length = 500)
    private String observaciones;

    @NotNull(message = "La fecha de registro es obligatoria")
    @Column(name = "FECHA_REGISTRO", nullable = false)
    private LocalDateTime fechaRegistro;

    @NotBlank(message = "El registrador es obligatorio")
    @Size(max = 100, message = "El nombre del registrador no puede exceder 100 caracteres")
    @Column(name = "REGISTRADO_POR", nullable = false, length = 100)
    private String registradoPor;

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    /**
     * Constructor sin ID
     */
    public SignosVitales(Paciente paciente, Integer frecuenciaCardiaca, Integer presionSistolica,
                        Integer presionDiastolica, BigDecimal temperatura, Integer saturacionOxigeno,
                        Integer frecuenciaRespiratoria, String estadoConciencia, String observaciones,
                        LocalDateTime fechaRegistro, String registradoPor) {
        this.paciente = paciente;
        this.frecuenciaCardiaca = frecuenciaCardiaca;
        this.presionSistolica = presionSistolica;
        this.presionDiastolica = presionDiastolica;
        this.temperatura = temperatura;
        this.saturacionOxigeno = saturacionOxigeno;
        this.frecuenciaRespiratoria = frecuenciaRespiratoria;
        this.estadoConciencia = estadoConciencia;
        this.observaciones = observaciones;
        this.fechaRegistro = fechaRegistro;
        this.registradoPor = registradoPor;
    }

    @PrePersist
    protected void onCreate() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
        if (estadoConciencia == null) {
            estadoConciencia = "ALERTA";
        }
    }

    /**
     * Métodos para verificar si los valores están en rango normal
     */
    public boolean frecuenciaCardiacaNormal() {
        return frecuenciaCardiaca >= 60 && frecuenciaCardiaca <= 100;
    }

    public boolean frecuenciaCardiacaCritica() {
        return frecuenciaCardiaca > 120 || frecuenciaCardiaca < 50;
    }

    public boolean presionSistolicaNormal() {
        return presionSistolica >= 90 && presionSistolica <= 140;
    }

    public boolean presionSistolicaCritica() {
        return presionSistolica > 160 || presionSistolica < 80;
    }

    public boolean presionDiastolicaNormal() {
        return presionDiastolica >= 60 && presionDiastolica <= 90;
    }

    public boolean presionDiastolicaCritica() {
        return presionDiastolica > 100 || presionDiastolica < 50;
    }

    public boolean temperaturaNormal() {
        return temperatura.compareTo(new BigDecimal("36.0")) >= 0 && 
               temperatura.compareTo(new BigDecimal("37.5")) <= 0;
    }

    public boolean temperaturaCritica() {
        return temperatura.compareTo(new BigDecimal("38.5")) > 0 || 
               temperatura.compareTo(new BigDecimal("35.0")) < 0;
    }

    public boolean saturacionOxigenoNormal() {
        return saturacionOxigeno >= 95 && saturacionOxigeno <= 100;
    }

    public boolean saturacionOxigenoCritica() {
        return saturacionOxigeno < 90;
    }

    public boolean frecuenciaRespiratoriaNormal() {
        return frecuenciaRespiratoria >= 12 && frecuenciaRespiratoria <= 20;
    }

    public boolean frecuenciaRespiratoriaCritica() {
        return frecuenciaRespiratoria > 25 || frecuenciaRespiratoria < 10;
    }

    /**
     * Verifica si hay algún signo vital en estado crítico
     */
    public boolean tieneSignoCritico() {
        return frecuenciaCardiacaCritica() ||
               presionSistolicaCritica() ||
               presionDiastolicaCritica() ||
               temperaturaCritica() ||
               saturacionOxigenoCritica() ||
               frecuenciaRespiratoriaCritica();
    }
}
