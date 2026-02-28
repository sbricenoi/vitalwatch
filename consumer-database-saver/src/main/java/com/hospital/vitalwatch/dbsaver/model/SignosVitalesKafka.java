package com.hospital.vitalwatch.dbsaver.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "SIGNOS_VITALES_KAFKA")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignosVitalesKafka {

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

    @Column(name = "paciente_id", nullable = false, length = 50)
    private String pacienteId;

    @Column(name = "paciente_nombre", nullable = false, length = 200)
    private String pacienteNombre;

    @Column(name = "sala", length = 50)
    private String sala;

    @Column(name = "cama", length = 20)
    private String cama;

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

    @Column(name = "device_id", length = 50)
    private String deviceId;

    @Column(name = "timestamp_medicion", nullable = false)
    private LocalDateTime timestampMedicion;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
