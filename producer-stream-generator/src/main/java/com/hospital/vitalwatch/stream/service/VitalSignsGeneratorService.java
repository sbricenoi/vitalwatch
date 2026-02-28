package com.hospital.vitalwatch.stream.service;

import com.hospital.vitalwatch.stream.dto.VitalSignsMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
public class VitalSignsGeneratorService {

    private final Random random = new Random();
    
    private final List<PatientInfo> patients = List.of(
        new PatientInfo("P001", "Juan Pérez", "UCI-A", "101", "DEVICE-001"),
        new PatientInfo("P002", "María García", "UCI-A", "102", "DEVICE-002"),
        new PatientInfo("P003", "Carlos López", "UCI-B", "201", "DEVICE-003"),
        new PatientInfo("P004", "Ana Martínez", "UCI-B", "202", "DEVICE-004"),
        new PatientInfo("P005", "Pedro Sánchez", "UCI-C", "301", "DEVICE-005")
    );

    public VitalSignsMessage generateRandomVitalSigns() {
        PatientInfo patient = patients.get(random.nextInt(patients.size()));
        
        boolean generateAnomalies = random.nextDouble() < 0.15;
        
        VitalSignsMessage message = VitalSignsMessage.builder()
            .messageId(UUID.randomUUID().toString())
            .pacienteId(patient.id)
            .pacienteNombre(patient.nombre)
            .sala(patient.sala)
            .cama(patient.cama)
            .deviceId(patient.deviceId)
            .frecuenciaCardiaca(generateFrecuenciaCardiaca(generateAnomalies))
            .presionSistolica(generatePresionSistolica(generateAnomalies))
            .presionDiastolica(generatePresionDiastolica(generateAnomalies))
            .temperatura(generateTemperatura(generateAnomalies))
            .saturacionOxigeno(generateSaturacionOxigeno(generateAnomalies))
            .frecuenciaRespiratoria(generateFrecuenciaRespiratoria(generateAnomalies))
            .timestamp(LocalDateTime.now())
            .source("STREAM_GENERATOR")
            .build();
        
        if (generateAnomalies) {
            log.debug("⚠️ Generando signos con anomalías para {}", patient.nombre);
        }
        
        return message;
    }

    private Integer generateFrecuenciaCardiaca(boolean anomaly) {
        if (anomaly && random.nextDouble() < 0.5) {
            return random.nextBoolean() ? 
                random.nextInt(40, 60) :
                random.nextInt(120, 160);
        }
        return random.nextInt(60, 100);
    }

    private Integer generatePresionSistolica(boolean anomaly) {
        if (anomaly && random.nextDouble() < 0.5) {
            return random.nextBoolean() ? 
                random.nextInt(70, 90) :
                random.nextInt(140, 190);
        }
        return random.nextInt(90, 130);
    }

    private Integer generatePresionDiastolica(boolean anomaly) {
        if (anomaly && random.nextDouble() < 0.5) {
            return random.nextBoolean() ? 
                random.nextInt(40, 60) :
                random.nextInt(90, 110);
        }
        return random.nextInt(60, 85);
    }

    private Double generateTemperatura(boolean anomaly) {
        if (anomaly && random.nextDouble() < 0.5) {
            return random.nextBoolean() ? 
                35.0 + random.nextDouble() :
                38.0 + random.nextDouble() * 2.5;
        }
        return 36.0 + random.nextDouble() * 1.5;
    }

    private Integer generateSaturacionOxigeno(boolean anomaly) {
        if (anomaly && random.nextDouble() < 0.5) {
            return random.nextInt(80, 94);
        }
        return random.nextInt(95, 100);
    }

    private Integer generateFrecuenciaRespiratoria(boolean anomaly) {
        if (anomaly && random.nextDouble() < 0.5) {
            return random.nextBoolean() ? 
                random.nextInt(8, 12) :
                random.nextInt(22, 30);
        }
        return random.nextInt(12, 20);
    }

    private static class PatientInfo {
        String id;
        String nombre;
        String sala;
        String cama;
        String deviceId;

        PatientInfo(String id, String nombre, String sala, String cama, String deviceId) {
            this.id = id;
            this.nombre = nombre;
            this.sala = sala;
            this.cama = cama;
            this.deviceId = deviceId;
        }
    }
}
