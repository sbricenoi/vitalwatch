package com.hospital.vitalwatch.dbsaver.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ALERTAS_KAFKA")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaKafka {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kafka_topic", nullable = false, length = 100)
    private String kafkaTopic;

    @Column(name = "kafka_partition", nullable = false)
    private Integer kafkaPartition;

    @Column(name = "kafka_offset", nullable = false)
    private Long kafkaOffset;

    @Column(name = "kafka_timestamp", nullable = false)
    private LocalDateTime kafkaTimestamp;

    @Column(name = "alert_id", unique = true, nullable = false, length = 100)
    private String alertId;

    @Column(name = "paciente_id", nullable = false, length = 50)
    private String pacienteId;

    @Column(name = "paciente_nombre", nullable = false, length = 200)
    private String pacienteNombre;

    @Column(name = "sala", length = 50)
    private String sala;

    @Column(name = "cama", length = 20)
    private String cama;

    @Column(name = "tipo_alerta", nullable = false, length = 100)
    private String tipoAlerta;

    @Lob
    @Column(name = "mensaje", nullable = false)
    private String mensaje;

    @Column(name = "severidad", nullable = false, length = 20)
    private String severidad;

    @Column(name = "frecuencia_cardiaca")
    private Integer frecuenciaCardiaca;

    @Column(name = "presion_sistolica")
    private Integer presionSistolica;

    @Column(name = "presion_diastolica")
    private Integer presionDiastolica;

    @Column(name = "temperatura", precision = 4, scale = 2)
    private Double temperatura;

    @Column(name = "saturacion_oxigeno")
    private Integer saturacionOxigeno;

    @Column(name = "frecuencia_respiratoria")
    private Integer frecuenciaRespiratoria;

    @Lob
    @Column(name = "anomalias")
    private String anomalias;

    @Column(name = "cantidad_anomalias")
    private Integer cantidadAnomalias;

    @Column(name = "device_id", length = 50)
    private String deviceId;

    @Column(name = "estado", length = 20)
    private String estado;

    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (estado == null) {
            estado = "ACTIVA";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
