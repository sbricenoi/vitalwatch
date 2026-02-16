package com.hospital.vitalwatch.jsongenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * VitalWatch - Consumidor 2: Generador de Archivos JSON
 * 
 * Este microservicio consume alertas de RabbitMQ (cola: vital-signs-alerts)
 * y genera archivos JSON individuales en el sistema de archivos.
 */
@SpringBootApplication
public class JsonGeneratorConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(JsonGeneratorConsumerApplication.class, args);
    }
}
