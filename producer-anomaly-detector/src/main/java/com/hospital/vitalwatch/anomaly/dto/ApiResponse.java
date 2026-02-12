package com.hospital.vitalwatch.anomaly.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta estándar de la API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    private String code;
    private String message;
    private T data;
    private String traceId;

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("200", message, data, java.util.UUID.randomUUID().toString());
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>("201", message, data, java.util.UUID.randomUUID().toString());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("500", message, null, java.util.UUID.randomUUID().toString());
    }

    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>("400", message, null, java.util.UUID.randomUUID().toString());
    }
}
