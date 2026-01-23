package com.hospital.vitalwatch.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI/Swagger para documentación de la API
 * Accesible en: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI vitalWatchOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("VitalWatch API")
                        .description("""
                                API REST para el Sistema de Monitoreo y Alerta de Signos Vitales VitalWatch.
                                
                                ## Características principales:
                                - Gestión completa de pacientes
                                - Registro y seguimiento de signos vitales
                                - Sistema de alertas automático basado en rangos
                                - Dashboard con estadísticas en tiempo real
                                
                                ## Rangos normales de signos vitales:
                                - **Frecuencia Cardíaca**: 60-100 bpm
                                - **Presión Arterial**: Sistólica 90-140, Diastólica 60-90 mmHg
                                - **Temperatura**: 36.5-37.5 °C
                                - **Saturación de Oxígeno**: 95-100%
                                - **Frecuencia Respiratoria**: 12-20 rpm
                                - **Glucosa**: 70-140 mg/dL
                                
                                ## Alertas automáticas:
                                El sistema genera automáticamente alertas cuando los signos vitales
                                están fuera de los rangos normales, clasificándolas por severidad
                                (BAJA, MODERADA, CRÍTICA).
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo VitalWatch")
                                .email("soporte@vitalwatch.com")
                                .url("https://vitalwatch.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de Desarrollo"),
                        new Server()
                                .url("https://api.vitalwatch.com")
                                .description("Servidor de Producción")
                ));
    }
}
