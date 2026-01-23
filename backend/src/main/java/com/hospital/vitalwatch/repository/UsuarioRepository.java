package com.hospital.vitalwatch.repository;

import com.hospital.vitalwatch.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Usuario
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    /**
     * Busca un usuario por su email
     */
    Optional<Usuario> findByEmail(String email);
    
    /**
     * Verifica si existe un usuario con el email especificado
     */
    boolean existsByEmail(String email);
    
    /**
     * Busca un usuario por email y que esté activo
     */
    Optional<Usuario> findByEmailAndActivoTrue(String email);
}
