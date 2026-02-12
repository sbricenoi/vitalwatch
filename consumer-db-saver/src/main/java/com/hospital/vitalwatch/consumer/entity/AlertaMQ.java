package com.hospital.vitalwatch.consumer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa una alerta guardada en Oracle
 * Tabla: ALERTAS_MQ
 */
@Entity
@Table(name = "ALERTAS_MQ")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertaMQ {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "alertas_seq")
    @SequenceGenerator(name = "alertas_seq", sequenceName = "SEQ_ALERTAS_MQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "PACIENTE_ID", nullable = false)
    private Long pacienteId;

    @Column(name = "PACIENTE_NOMBRE", length = 100, nullable = false)
    private String pacienteNombre;

    @Column(name = "SALA", length = 50, nullable = false)
    private String sala;

    @Column(name = "CAMA", length = 50, nullable = false)
    private String cama;

    @Column(name = "ANOMALIAS_JSON", columnDefinition = "CLOB", nullable = false)
    private String anomaliasJson;

    @Column(name = "ANOMALIAS_COUNT", nullable = false)
    private Integer anomaliasCount;

    @Column(name = "SEVERITY", length = 20, nullable = false)
    private String severity;

    @Column(name = "FRECUENCIA_CARDIACA")
    private Integer frecuenciaCardiaca;

    @Column(name = "PRESION_SISTOLICA")
    private Integer presionSistolica;

    @Column(name = "PRESION_DIASTOLICA")
    private Integer presionDiastolica;

    @Column(name = "TEMPERATURA", length = 10)
    private String temperatura;

    @Column(name = "SATURACION_OXIGENO")
    private Integer saturacionOxigeno;

    @Column(name = "FRECUENCIA_RESPIRATORIA")
    private Integer frecuenciaRespiratoria;

    @Column(name = "DEVICE_ID", length = 100)
    private String deviceId;

    @Column(name = "DETECTED_AT", nullable = false)
    private LocalDateTime detectedAt;

    @Column(name = "SAVED_AT", nullable = false)
    private LocalDateTime savedAt;

    @Column(name = "QUEUE_NAME", length = 100, nullable = false)
    private String queueName;
}
