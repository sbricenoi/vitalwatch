package com.hospital.vitalwatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * VitalWatch - Sistema de Alertas Médicas en Tiempo Real
 * 
 * Aplicación principal Spring Boot que gestiona el monitoreo de pacientes críticos
 * y generación de alertas médicas automáticas basadas en signos vitales.
 * 
 * @author Equipo VitalWatch
 * @version 1.0.0
 * @since 2026-01-22
 */
@SpringBootApplication
@EnableJpaAuditing
public class VitalWatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(VitalWatchApplication.class, args);
        System.out.println("""
            
            ╔══════════════════════════════════════════════════════════════╗
            ║                                                              ║
            ║              VitalWatch Backend API                          ║
            ║    Sistema de Alertas Médicas en Tiempo Real                ║
            ║                                                              ║
            ║    Estado: ✅ INICIADO CORRECTAMENTE                         ║
            ║    Puerto: 8080                                              ║
            ║    Swagger: http://localhost:8080/swagger-ui.html           ║
            ║    Health: http://localhost:8080/actuator/health            ║
            ║                                                              ║
            ╚══════════════════════════════════════════════════════════════╝
            
            """);
    }
}
