package com.hospital.vitalwatch.controller;

import com.hospital.vitalwatch.dto.ApiResponse;
import com.hospital.vitalwatch.dto.LoginRequest;
import com.hospital.vitalwatch.dto.LoginResponse;
import com.hospital.vitalwatch.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para Autenticación
 * Maneja las operaciones de login y logout
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticación", description = "API para autenticación de usuarios")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint de login
     * POST /api/v1/auth/login
     * 
     * @param request Datos de login (email y password)
     * @return LoginResponse con token y datos del usuario
     */
    @PostMapping("/login")
    @Operation(
        summary = "Login de usuario",
        description = "Autentica un usuario y devuelve un token de acceso. Credenciales de prueba disponibles en la documentación."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Login exitoso",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Credenciales inválidas"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "No autorizado"
        )
    })
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        
        log.info("POST /api/v1/auth/login - Intento de login para: {}", request.getEmail());
        
        LoginResponse response = authService.login(request);
        
        return ResponseEntity.ok(
            ApiResponse.success("Login exitoso", response)
        );
    }

    /**
     * Endpoint de verificación de estado de autenticación
     * GET /api/v1/auth/check
     * 
     * @return Estado de la autenticación
     */
    @GetMapping("/check")
    @Operation(
        summary = "Verificar autenticación",
        description = "Verifica si el usuario está autenticado (endpoint de prueba)"
    )
    public ResponseEntity<ApiResponse<String>> checkAuth() {
        log.info("GET /api/v1/auth/check - Verificación de autenticación");
        
        return ResponseEntity.ok(
            ApiResponse.success("Usuario autenticado", "authenticated")
        );
    }

    /**
     * Endpoint de información de credenciales de prueba
     * GET /api/v1/auth/credentials
     * 
     * @return Credenciales de prueba disponibles
     */
    @GetMapping("/credentials")
    @Operation(
        summary = "Obtener credenciales de prueba",
        description = "Devuelve las credenciales de usuarios de prueba disponibles para login"
    )
    public ResponseEntity<ApiResponse<Object>> getTestCredentials() {
        log.info("GET /api/v1/auth/credentials - Obteniendo credenciales de prueba");
        
        Object credentials = new Object() {
            public final Object admin = new Object() {
                public final String email = "admin@vitalwatch.com";
                public final String password = "Admin123!";
                public final String rol = "ADMIN";
            };
            public final Object medico = new Object() {
                public final String email = "medico@vitalwatch.com";
                public final String password = "Medico123!";
                public final String rol = "MEDICO";
            };
            public final Object enfermera = new Object() {
                public final String email = "enfermera@vitalwatch.com";
                public final String password = "Enfermera123!";
                public final String rol = "ENFERMERA";
            };
        };
        
        return ResponseEntity.ok(
            ApiResponse.success("Credenciales de prueba obtenidas", credentials)
        );
    }
}
