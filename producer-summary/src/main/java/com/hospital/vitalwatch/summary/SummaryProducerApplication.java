package com.hospital.vitalwatch.summary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * VitalWatch - Productor 2: Generador de Resúmenes Periódicos
 * 
 * Este microservicio genera resúmenes periódicos (cada 5 minutos) de
 * las señales vitales y estadísticas del sistema, publicándolos a RabbitMQ.
 */
@SpringBootApplication
@EnableScheduling
public class SummaryProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SummaryProducerApplication.class, args);
    }
}
