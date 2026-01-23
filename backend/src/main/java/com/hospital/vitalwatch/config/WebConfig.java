package com.hospital.vitalwatch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración adicional de Web MVC
 * Configuraciones personalizadas para el manejo de requests HTTP
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    // Aquí se pueden agregar configuraciones adicionales como:
    // - Interceptors personalizados
    // - Conversores de datos
    // - Validadores personalizados
    // - Configuración de Locale
    // - Etc.
}
