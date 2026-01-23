-- ============================================================================
-- VitalWatch - Sistema de Alertas Médicas en Tiempo Real
-- Script de Creación de Base de Datos Oracle
-- ============================================================================

-- Eliminar tablas si existen (para desarrollo)
DROP TABLE IF EXISTS alertas CASCADE CONSTRAINTS;
DROP TABLE IF EXISTS signos_vitales CASCADE CONSTRAINTS;
DROP TABLE IF EXISTS pacientes CASCADE CONSTRAINTS;

-- Eliminar secuencias si existen
DROP SEQUENCE IF EXISTS seq_pacientes;
DROP SEQUENCE IF EXISTS seq_signos_vitales;
DROP SEQUENCE IF EXISTS seq_alertas;

-- ============================================================================
-- TABLA: PACIENTES
-- Descripción: Almacena información de pacientes hospitalizados
-- ============================================================================
CREATE TABLE pacientes (
    id NUMBER(19) PRIMARY KEY,
    nombre VARCHAR2(100) NOT NULL,
    apellido VARCHAR2(100) NOT NULL,
    rut VARCHAR2(12) UNIQUE NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    edad NUMBER(3) NOT NULL,
    genero VARCHAR2(1) CHECK (genero IN ('M', 'F', 'O')),
    sala VARCHAR2(20) NOT NULL,
    cama VARCHAR2(10) NOT NULL,
    estado VARCHAR2(20) DEFAULT 'ESTABLE' CHECK (estado IN ('ESTABLE', 'MODERADO', 'CRÍTICO', 'RECUPERACIÓN')),
    diagnostico VARCHAR2(500),
    fecha_ingreso TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    fecha_alta TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Índices para optimización
CREATE INDEX idx_pacientes_rut ON pacientes(rut);
CREATE INDEX idx_pacientes_estado ON pacientes(estado);
CREATE INDEX idx_pacientes_sala ON pacientes(sala);

-- Secuencia para IDs
CREATE SEQUENCE seq_pacientes START WITH 1 INCREMENT BY 1;

-- ============================================================================
-- TABLA: SIGNOS_VITALES
-- Descripción: Registro de signos vitales de pacientes
-- ============================================================================
CREATE TABLE signos_vitales (
    id NUMBER(19) PRIMARY KEY,
    paciente_id NUMBER(19) NOT NULL,
    frecuencia_cardiaca NUMBER(3) NOT NULL CHECK (frecuencia_cardiaca >= 0 AND frecuencia_cardiaca <= 300),
    presion_sistolica NUMBER(3) NOT NULL CHECK (presion_sistolica >= 0 AND presion_sistolica <= 300),
    presion_diastolica NUMBER(3) NOT NULL CHECK (presion_diastolica >= 0 AND presion_diastolica <= 200),
    temperatura NUMBER(4,2) NOT NULL CHECK (temperatura >= 30.0 AND temperatura <= 45.0),
    saturacion_oxigeno NUMBER(3) NOT NULL CHECK (saturacion_oxigeno >= 0 AND saturacion_oxigeno <= 100),
    frecuencia_respiratoria NUMBER(3) NOT NULL CHECK (frecuencia_respiratoria >= 0 AND frecuencia_respiratoria <= 100),
    estado_conciencia VARCHAR2(50) DEFAULT 'ALERTA' CHECK (estado_conciencia IN ('ALERTA', 'VERBAL', 'DOLOR', 'INCONSCIENTE')),
    observaciones VARCHAR2(500),
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    registrado_por VARCHAR2(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    -- Foreign Key
    CONSTRAINT fk_signos_paciente FOREIGN KEY (paciente_id) 
        REFERENCES pacientes(id) ON DELETE CASCADE
);

-- Índices para optimización
CREATE INDEX idx_signos_paciente ON signos_vitales(paciente_id);
CREATE INDEX idx_signos_fecha ON signos_vitales(fecha_registro);

-- Secuencia para IDs
CREATE SEQUENCE seq_signos_vitales START WITH 1 INCREMENT BY 1;

-- ============================================================================
-- TABLA: ALERTAS
-- Descripción: Registro de alertas médicas generadas automáticamente
-- ============================================================================
CREATE TABLE alertas (
    id NUMBER(19) PRIMARY KEY,
    paciente_id NUMBER(19) NOT NULL,
    tipo VARCHAR2(50) NOT NULL,
    mensaje VARCHAR2(500) NOT NULL,
    severidad VARCHAR2(20) DEFAULT 'MODERADA' CHECK (severidad IN ('BAJA', 'MODERADA', 'CRÍTICA')),
    estado VARCHAR2(20) DEFAULT 'ACTIVA' CHECK (estado IN ('ACTIVA', 'RESUELTA', 'DESCARTADA')),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    fecha_resolucion TIMESTAMP,
    resuelto_por VARCHAR2(100),
    notas_resolucion VARCHAR2(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    -- Foreign Key
    CONSTRAINT fk_alertas_paciente FOREIGN KEY (paciente_id) 
        REFERENCES pacientes(id) ON DELETE CASCADE
);

-- Índices para optimización
CREATE INDEX idx_alertas_paciente ON alertas(paciente_id);
CREATE INDEX idx_alertas_estado ON alertas(estado);
CREATE INDEX idx_alertas_severidad ON alertas(severidad);
CREATE INDEX idx_alertas_fecha ON alertas(fecha_creacion);

-- Secuencia para IDs
CREATE SEQUENCE seq_alertas START WITH 1 INCREMENT BY 1;

-- ============================================================================
-- TRIGGERS PARA ACTUALIZACIÓN AUTOMÁTICA DE updated_at
-- ============================================================================

-- Trigger para PACIENTES
CREATE OR REPLACE TRIGGER trg_pacientes_updated_at
BEFORE UPDATE ON pacientes
FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/

-- Trigger para SIGNOS_VITALES
CREATE OR REPLACE TRIGGER trg_signos_updated_at
BEFORE UPDATE ON signos_vitales
FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/

-- Trigger para ALERTAS
CREATE OR REPLACE TRIGGER trg_alertas_updated_at
BEFORE UPDATE ON alertas
FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/

-- ============================================================================
-- COMENTARIOS EN TABLAS Y COLUMNAS (Documentación)
-- ============================================================================

COMMENT ON TABLE pacientes IS 'Registro de pacientes hospitalizados';
COMMENT ON COLUMN pacientes.estado IS 'Estados: ESTABLE, MODERADO, CRÍTICO, RECUPERACIÓN';

COMMENT ON TABLE signos_vitales IS 'Registro histórico de signos vitales de pacientes';
COMMENT ON COLUMN signos_vitales.estado_conciencia IS 'Escala AVDI: ALERTA, VERBAL, DOLOR, INCONSCIENTE';

COMMENT ON TABLE alertas IS 'Alertas médicas generadas automáticamente por el sistema';
COMMENT ON COLUMN alertas.severidad IS 'Niveles: BAJA, MODERADA, CRÍTICA';
COMMENT ON COLUMN alertas.estado IS 'Estados: ACTIVA, RESUELTA, DESCARTADA';

-- ============================================================================
-- PERMISOS (Ajustar según usuario de aplicación)
-- ============================================================================

-- GRANT SELECT, INSERT, UPDATE, DELETE ON pacientes TO app_user;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON signos_vitales TO app_user;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON alertas TO app_user;
-- GRANT SELECT ON seq_pacientes TO app_user;
-- GRANT SELECT ON seq_signos_vitales TO app_user;
-- GRANT SELECT ON seq_alertas TO app_user;

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================

-- Verificación de creación de objetos
SELECT 'Tabla creada: ' || table_name FROM user_tables 
WHERE table_name IN ('PACIENTES', 'SIGNOS_VITALES', 'ALERTAS');

SELECT 'Secuencia creada: ' || sequence_name FROM user_sequences 
WHERE sequence_name IN ('SEQ_PACIENTES', 'SEQ_SIGNOS_VITALES', 'SEQ_ALERTAS');

COMMIT;
