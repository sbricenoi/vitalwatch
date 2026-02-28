package com.hospital.vitalwatch.dbsaver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DatabaseSaverApplication {

    public static void main(String[] args) {
        SpringApplication.run(DatabaseSaverApplication.class, args);
        System.out.println("✅ VitalWatch Database Saver Consumer iniciado");
        System.out.println("💾 Guardando stream de signos vitales y alertas en Oracle Cloud...");
    }
}
