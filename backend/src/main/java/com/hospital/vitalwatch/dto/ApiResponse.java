package com.hospital.vitalwatch.dto;

import com.hospital.vitalwatch.util.TraceIdGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO para respuestas estándar de la API
 * Todas las respuestas siguen este formato según requerimientos del proyecto
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    /**
     * ID único de seguimiento de la petición
     */
    private String traceId;
    
    /**
     * Código HTTP de la respuesta
     */
    private String code;
    
    /**
     * Mensaje descriptivo de la respuesta
     */
    private String message;
    
    /**
     * Datos de la respuesta (puede ser null)
     */
    private T data;

    /**
     * Constructor para respuestas exitosas con datos
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(
            TraceIdGenerator.generate(),
            "200",
            message,
            data
        );
    }

    /**
     * Constructor para respuestas creadas (201)
     */
    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(
            TraceIdGenerator.generate(),
            "201",
            message,
            data
        );
    }

    /**
     * Constructor para respuestas sin contenido
     */
    public static <T> ApiResponse<T> noContent(String message) {
        return new ApiResponse<>(
            UUID.randomUUID().toString(),
            "204",
            message,
            null
        );
    }

    /**
     * Constructor para errores de validación (400)
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(
            UUID.randomUUID().toString(),
            "400",
            message,
            null
        );
    }

    /**
     * Constructor para no encontrado (404)
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(
            UUID.randomUUID().toString(),
            "404",
            message,
            null
        );
    }

    /**
     * Constructor para error del servidor (500)
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(
            UUID.randomUUID().toString(),
            "500",
            message,
            null
        );
    }

    /**
     * Constructor genérico con código personalizado
     */
    public static <T> ApiResponse<T> custom(String code, String message, T data) {
        return new ApiResponse<>(
            TraceIdGenerator.generate(),
            code,
            message,
            data
        );
    }

    /**
     * Constructor para error con código numérico y datos
     */
    public static <T> ApiResponse<T> error(String message, T data, Integer code) {
        return new ApiResponse<>(
            TraceIdGenerator.generate(),
            String.valueOf(code),
            message,
            data
        );
    }
}
