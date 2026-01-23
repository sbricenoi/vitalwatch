package com.hospital.vitalwatch.exception;

/**
 * Excepción lanzada cuando un recurso no es encontrado
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s con ID %d no encontrado", resourceName, id));
    }
    
    public ResourceNotFoundException(String resourceName, String field, Object value) {
        super(String.format("%s con %s='%s' no encontrado", resourceName, field, value));
    }
}
