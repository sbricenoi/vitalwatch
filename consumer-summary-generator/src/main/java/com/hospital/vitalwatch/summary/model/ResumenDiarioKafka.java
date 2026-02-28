package com.hospital.vitalwatch.summary.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "RESUMEN_DIARIO_KAFKA")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenDiarioKafka {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha", nullable = false, unique = true)
    private LocalDate fecha;

    @Column(name = "total_pacientes_monitoreados")
    private Long totalPacientesMonitoreados;

    @Column(name = "pacientes_con_alertas")
    private Long pacientesConAlertas;

    @Column(name = "total_mediciones")
    private Long totalMediciones;

    @Column(name = "mediciones_por_hora", precision = 10, scale = 2)
    private Double medicionesPorHora;

    @Column(name = "total_alertas")
    private Long totalAlertas;

    @Column(name = "alertas_criticas")
    private Long alertasCriticas;

    @Column(name = "alertas_altas")
    private Long alertasAltas;

    @Column(name = "alertas_moderadas")
    private Long alertasModeradas;

    @Column(name = "alertas_bajas")
    private Long alertasBajas;

    @Column(name = "promedio_frecuencia_cardiaca", precision = 5, scale = 2)
    private Double promedioFrecuenciaCardiaca;

    @Column(name = "promedio_presion_sistolica", precision = 5, scale = 2)
    private Double promedioPresionSistolica;

    @Column(name = "promedio_presion_diastolica", precision = 5, scale = 2)
    private Double promedioPresionDiastolica;

    @Column(name = "promedio_temperatura", precision = 4, scale = 2)
    private Double promedioTemperatura;

    @Column(name = "promedio_saturacion_oxigeno", precision = 5, scale = 2)
    private Double promedioSaturacionOxigeno;

    @Column(name = "promedio_frecuencia_respiratoria", precision = 5, scale = 2)
    private Double promedioFrecuenciaRespiratoria;

    @Column(name = "max_frecuencia_cardiaca")
    private Integer maxFrecuenciaCardiaca;

    @Column(name = "min_frecuencia_cardiaca")
    private Integer minFrecuenciaCardiaca;

    @Column(name = "max_temperatura", precision = 4, scale = 2)
    private Double maxTemperatura;

    @Column(name = "min_temperatura", precision = 4, scale = 2)
    private Double minTemperatura;

    @Column(name = "min_saturacion_oxigeno")
    private Integer minSaturacionOxigeno;

    @Column(name = "mensajes_procesados_vital_signs")
    private Long mensajesProcesadosVitalSigns;

    @Column(name = "mensajes_procesados_alertas")
    private Long mensajesProcesadosAlertas;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        ultimaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        ultimaActualizacion = LocalDateTime.now();
    }
}
