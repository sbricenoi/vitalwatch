package com.hospital.vitalwatch.util;

import java.util.UUID;

/**
 * Utilidad para generar IDs de trazabilidad únicos
 * Útil para seguimiento de requests y debugging
 */
public class TraceIdGenerator {
    
    /**
     * Genera un ID único para tracing
     * @return String con formato UUID
     */
    public static String generate() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Genera un ID corto (primeros 8 caracteres del UUID)
     * @return String con ID corto
     */
    public static String generateShort() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
