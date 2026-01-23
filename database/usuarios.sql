-- ============================================================================
-- VitalWatch - Script de Usuarios para Autenticación
-- ============================================================================
-- Descripción: Crea la tabla de usuarios y datos de prueba
-- Fecha: 23 de enero de 2026
-- ============================================================================

-- Eliminar tabla si existe (solo para desarrollo)
-- DROP TABLE USUARIOS CASCADE CONSTRAINTS;

-- Crear tabla de usuarios
CREATE TABLE USUARIOS (
    ID NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    EMAIL VARCHAR2(100) NOT NULL UNIQUE,
    PASSWORD VARCHAR2(255) NOT NULL,
    NOMBRE VARCHAR2(100) NOT NULL,
    ROL VARCHAR2(20) NOT NULL CHECK (ROL IN ('ADMIN', 'MEDICO', 'ENFERMERA')),
    ACTIVO NUMBER(1) DEFAULT 1,
    FECHA_CREACION TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FECHA_ACTUALIZACION TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear índice para búsqueda rápida por email
CREATE INDEX IDX_USUARIOS_EMAIL ON USUARIOS(EMAIL);

-- Comentarios en las columnas
COMMENT ON TABLE USUARIOS IS 'Tabla de usuarios del sistema VitalWatch';
COMMENT ON COLUMN USUARIOS.ID IS 'Identificador único del usuario';
COMMENT ON COLUMN USUARIOS.EMAIL IS 'Email del usuario (usado para login)';
COMMENT ON COLUMN USUARIOS.PASSWORD IS 'Contraseña del usuario (texto plano para demo)';
COMMENT ON COLUMN USUARIOS.NOMBRE IS 'Nombre completo del usuario';
COMMENT ON COLUMN USUARIOS.ROL IS 'Rol del usuario: ADMIN, MEDICO, ENFERMERA';
COMMENT ON COLUMN USUARIOS.ACTIVO IS 'Estado del usuario (1=activo, 0=inactivo)';
COMMENT ON COLUMN USUARIOS.FECHA_CREACION IS 'Fecha de creación del registro';
COMMENT ON COLUMN USUARIOS.FECHA_ACTUALIZACION IS 'Fecha de última actualización';

-- ============================================================================
-- DATOS DE PRUEBA
-- ============================================================================
-- IMPORTANTE: Las contraseñas están en texto plano solo para DEMOSTRACIÓN
-- En producción, se debe usar BCrypt o similar
-- ============================================================================

-- Usuario Administrador
INSERT INTO USUARIOS (EMAIL, PASSWORD, NOMBRE, ROL, ACTIVO) 
VALUES ('admin@vitalwatch.com', 'Admin123!', 'Administrador Principal', 'ADMIN', 1);

-- Usuario Médico
INSERT INTO USUARIOS (EMAIL, PASSWORD, NOMBRE, ROL, ACTIVO) 
VALUES ('medico@vitalwatch.com', 'Medico123!', 'Dr. Juan Pérez', 'MEDICO', 1);

-- Usuario Enfermera
INSERT INTO USUARIOS (EMAIL, PASSWORD, NOMBRE, ROL, ACTIVO) 
VALUES ('enfermera@vitalwatch.com', 'Enfermera123!', 'Enf. María González', 'ENFERMERA', 1);

-- Más usuarios de prueba
INSERT INTO USUARIOS (EMAIL, PASSWORD, NOMBRE, ROL, ACTIVO) 
VALUES ('doctor.lopez@vitalwatch.com', 'Doctor123!', 'Dr. Carlos López', 'MEDICO', 1);

INSERT INTO USUARIOS (EMAIL, PASSWORD, NOMBRE, ROL, ACTIVO) 
VALUES ('enfermera.silva@vitalwatch.com', 'Enfermera123!', 'Enf. Ana Silva', 'ENFERMERA', 1);

-- Commit de los cambios
COMMIT;

-- ============================================================================
-- VERIFICACIÓN
-- ============================================================================

-- Ver todos los usuarios creados
SELECT ID, EMAIL, NOMBRE, ROL, ACTIVO, FECHA_CREACION
FROM USUARIOS
ORDER BY ID;

-- Contar usuarios por rol
SELECT ROL, COUNT(*) AS TOTAL
FROM USUARIOS
WHERE ACTIVO = 1
GROUP BY ROL
ORDER BY ROL;

-- ============================================================================
-- CREDENCIALES DE PRUEBA PARA LOGIN
-- ============================================================================
/*
ADMIN:
  Email: admin@vitalwatch.com
  Password: Admin123!

MÉDICO:
  Email: medico@vitalwatch.com
  Password: Medico123!
  
  Email: doctor.lopez@vitalwatch.com
  Password: Doctor123!

ENFERMERA:
  Email: enfermera@vitalwatch.com
  Password: Enfermera123!
  
  Email: enfermera.silva@vitalwatch.com
  Password: Enfermera123!
*/

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================
