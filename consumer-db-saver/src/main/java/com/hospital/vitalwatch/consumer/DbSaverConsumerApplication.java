package com.hospital.vitalwatch.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * VitalWatch - Consumidor 1: Guardador de Alertas en Oracle Cloud
 * 
 * Este microservicio consume alertas de RabbitMQ (cola: vital-signs-alerts)
 * y las persiste en la base de datos Oracle Cloud Autonomous Database.
 */
@SpringBootApplication
public class DbSaverConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DbSaverConsumerApplication.class, args);
    }
}
