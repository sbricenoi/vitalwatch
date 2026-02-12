package com.hospital.vitalwatch.anomaly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * VitalWatch - Productor: Detector de Anomalías en Signos Vitales
 * 
 * Este microservicio recibe signos vitales de dispositivos médicos
 * y detecta anomalías. Cuando encuentra valores fuera de rangos normales,
 * publica una alerta a RabbitMQ para procesamiento asíncrono.
 */
@SpringBootApplication
public class AnomalyDetectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnomalyDetectorApplication.class, args);
    }
}
