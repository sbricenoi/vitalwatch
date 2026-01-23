package com.hospital.vitalwatch.service;

import com.hospital.vitalwatch.dto.LoginRequest;
import com.hospital.vitalwatch.dto.LoginResponse;
import com.hospital.vitalwatch.exception.ValidationException;
import com.hospital.vitalwatch.model.Usuario;
import com.hospital.vitalwatch.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.UUID;

/**
 * Servicio de Autenticación
 * Maneja el login y generación de tokens
 * 
 * NOTA: Esta es una implementación SIMPLIFICADA para demostración.
 * En producción se debe usar:
 * - BCrypt para passwords
 * - JWT con firma y expiración
 * - Refresh tokens
 * - Rate limiting
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Realiza el login de un usuario
     * @param request Datos de login (email y password)
     * @return LoginResponse con token y datos del usuario
     * @throws ValidationException si las credenciales son inválidas
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.info("Intento de login para email: {}", request.getEmail());
        
        // Buscar usuario por email (solo activos)
        Usuario usuario = usuarioRepository.findByEmailAndActivoTrue(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login fallido: Usuario no encontrado o inactivo - {}", request.getEmail());
                    return new ValidationException("Credenciales inválidas");
                });
        
        // Validar password (comparación simple para demo)
        // NOTA: En producción usar BCrypt: passwordEncoder.matches(password, usuario.getPassword())
        if (!request.getPassword().equals(usuario.getPassword())) {
            log.warn("Login fallido: Password incorrecto para usuario {}", request.getEmail());
            throw new ValidationException("Credenciales inválidas");
        }
        
        // Generar token simple
        String token = generateSimpleToken(usuario);
        
        log.info("Login exitoso para usuario: {} ({})", usuario.getNombre(), usuario.getRol());
        
        // Construir y retornar respuesta
        return LoginResponse.builder()
                .token(token)
                .tipo("Bearer")
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .build();
    }
    
    /**
     * Genera un token simple para el usuario
     * NOTA: En producción usar JWT (JSON Web Token) con:
     * - Firma digital
     * - Tiempo de expiración
     * - Claims personalizados
     * - Algoritmo HS256 o RS256
     * 
     * @param usuario Usuario para el cual generar el token
     * @return Token en formato Base64
     */
    private String generateSimpleToken(Usuario usuario) {
        // Token simple: Base64(id:email:rol:uuid:timestamp)
        String data = String.format("%d:%s:%s:%s:%d",
                usuario.getId(),
                usuario.getEmail(),
                usuario.getRol(),
                UUID.randomUUID().toString(),
                System.currentTimeMillis()
        );
        
        return Base64.getEncoder().encodeToString(data.getBytes());
    }
    
    /**
     * Valida un token (implementación básica)
     * NOTA: En producción validar:
     * - Firma del JWT
     * - Expiración
     * - Claims
     * - Revocación
     * 
     * @param token Token a validar
     * @return true si el token es válido
     */
    public boolean validateToken(String token) {
        try {
            // Validación simple: verificar que se puede decodificar
            byte[] decoded = Base64.getDecoder().decode(token);
            String tokenData = new String(decoded);
            
            // Verificar formato básico: id:email:rol:uuid:timestamp
            String[] parts = tokenData.split(":");
            return parts.length == 5;
            
        } catch (Exception e) {
            log.warn("Token inválido: {}", e.getMessage());
            return false;
        }
    }
}
