package com.hospital.vitalwatch.alertprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AlertProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlertProcessorApplication.class, args);
        System.out.println("✅ VitalWatch Alert Processor iniciado");
        System.out.println("🔍 Procesando stream de signos vitales y detectando anomalías...");
    }
}
