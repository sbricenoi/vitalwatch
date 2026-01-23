package com.hospital.vitalwatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuesta de login
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    private String token;
    
    private String tipo = "Bearer";
    
    private Long id;
    
    private String nombre;
    
    private String email;
    
    private String rol;
}
