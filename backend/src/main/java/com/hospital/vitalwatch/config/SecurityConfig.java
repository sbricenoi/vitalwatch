package com.hospital.vitalwatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de Seguridad para VitalWatch
 * 
 * NOTA IMPORTANTE:
 * Esta es una configuración BÁSICA para desarrollo y pruebas.
 * Para producción, se debe implementar:
 * - JWT para autenticación
 * - Roles y permisos (ADMIN, MEDICO, ENFERMERA)
 * - Integración con IdaaS (Identity as a Service)
 * - Rate limiting
 * - Auditoría de accesos
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configuración de la cadena de filtros de seguridad
     * 
     * DESARROLLO: Todos los endpoints están abiertos para facilitar pruebas
     * PRODUCCIÓN: Implementar autenticación JWT y autorización por roles
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilitar CSRF (necesario para APIs REST stateless)
            .csrf(csrf -> csrf.disable())
            
            // Configurar CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Política de sesión: Stateless (sin sesiones)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Configuración de autorización
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos (Swagger, Health Check, Auth)
                .requestMatchers(
                    "/api/v1/health/**",
                    "/api/v1/auth/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/actuator/**"
                ).permitAll()
                
                // TODO: En producción, proteger estos endpoints
                // Ejemplo con roles:
                // .requestMatchers("/api/v1/pacientes/**").hasAnyRole("MEDICO", "ENFERMERA", "ADMIN")
                // .requestMatchers("/api/v1/signos-vitales/**").hasAnyRole("MEDICO", "ENFERMERA")
                // .requestMatchers("/api/v1/alertas/**").hasAnyRole("MEDICO", "ENFERMERA", "ADMIN")
                // .requestMatchers("/api/v1/dashboard/**").authenticated()
                
                // POR AHORA: Permitir todos los endpoints (solo para desarrollo)
                .anyRequest().permitAll()
            );

        return http.build();
    }

    /**
     * Configuración de CORS (Cross-Origin Resource Sharing)
     * Permite que el frontend Angular acceda a la API
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Orígenes permitidos (en producción, especificar dominios exactos)
        configuration.setAllowedOriginPatterns(List.of("*"));
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // Headers expuestos
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials",
            "Authorization"
        ));
        
        // Permitir credenciales
        configuration.setAllowCredentials(true);
        
        // Tiempo de caché para preflight requests
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
