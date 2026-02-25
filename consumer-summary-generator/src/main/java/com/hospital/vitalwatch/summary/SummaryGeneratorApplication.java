package com.hospital.vitalwatch.summary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SummaryGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(SummaryGeneratorApplication.class, args);
        System.out.println("✅ VitalWatch Summary Generator Consumer iniciado");
        System.out.println("📊 Generando resúmenes diarios del sistema...");
    }
}
