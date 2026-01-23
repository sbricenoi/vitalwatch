-- ============================================================================
-- VitalWatch - Sistema de Alertas Médicas en Tiempo Real
-- Script de Datos de Prueba
-- ============================================================================

-- ============================================================================
-- DATOS DE PRUEBA: PACIENTES
-- ============================================================================

INSERT INTO pacientes (id, nombre, apellido, rut, fecha_nacimiento, edad, genero, sala, cama, estado, diagnostico, fecha_ingreso)
VALUES (seq_pacientes.NEXTVAL, 'Juan', 'Pérez', '12345678-9', TO_DATE('1958-05-15', 'YYYY-MM-DD'), 65, 'M', 'UCI', 'A-01', 'CRÍTICO', 'Insuficiencia cardíaca congestiva', CURRENT_TIMESTAMP - 5);

INSERT INTO pacientes (id, nombre, apellido, rut, fecha_nacimiento, edad, genero, sala, cama, estado, diagnostico, fecha_ingreso)
VALUES (seq_pacientes.NEXTVAL, 'María', 'González', '23456789-0', TO_DATE('1975-08-22', 'YYYY-MM-DD'), 48, 'F', 'UCI', 'A-02', 'MODERADO', 'Neumonía bilateral', CURRENT_TIMESTAMP - 3);

INSERT INTO pacientes (id, nombre, apellido, rut, fecha_nacimiento, edad, genero, sala, cama, estado, diagnostico, fecha_ingreso)
VALUES (seq_pacientes.NEXTVAL, 'Carlos', 'Rodríguez', '34567890-1', TO_DATE('1962-03-10', 'YYYY-MM-DD'), 61, 'M', 'UCI', 'A-03', 'CRÍTICO', 'Infarto agudo de miocardio', CURRENT_TIMESTAMP - 2);

INSERT INTO pacientes (id, nombre, apellido, rut, fecha_nacimiento, edad, genero, sala, cama, estado, diagnostico, fecha_ingreso)
VALUES (seq_pacientes.NEXTVAL, 'Ana', 'Martínez', '45678901-2', TO_DATE('1980-11-30', 'YYYY-MM-DD'), 43, 'F', 'UCI', 'B-01', 'MODERADO', 'Sepsis severa', CURRENT_TIMESTAMP - 4);

INSERT INTO pacientes (id, nombre, apellido, rut, fecha_nacimiento, edad, genero, sala, cama, estado, diagnostico, fecha_ingreso)
VALUES (seq_pacientes.NEXTVAL, 'Pedro', 'López', '56789012-3', TO_DATE('1970-07-18', 'YYYY-MM-DD'), 53, 'M', 'UCI', 'B-02', 'ESTABLE', 'Post operatorio cirugía mayor', CURRENT_TIMESTAMP - 1);

INSERT INTO pacientes (id, nombre, apellido, rut, fecha_nacimiento, edad, genero, sala, cama, estado, diagnostico, fecha_ingreso)
VALUES (seq_pacientes.NEXTVAL, 'Laura', 'Fernández', '67890123-4', TO_DATE('1985-02-25', 'YYYY-MM-DD'), 38, 'F', 'UCI', 'B-03', 'CRÍTICO', 'Falla respiratoria aguda', CURRENT_TIMESTAMP - 6);

INSERT INTO pacientes (id, nombre, apellido, rut, fecha_nacimiento, edad, genero, sala, cama, estado, diagnostico, fecha_ingreso)
VALUES (seq_pacientes.NEXTVAL, 'Roberto', 'Silva', '78901234-5', TO_DATE('1955-09-12', 'YYYY-MM-DD'), 68, 'M', 'UCI', 'C-01', 'MODERADO', 'EPOC descompensado', CURRENT_TIMESTAMP - 7);

INSERT INTO pacientes (id, nombre, apellido, rut, fecha_nacimiento, edad, genero, sala, cama, estado, diagnostico, fecha_ingreso)
VALUES (seq_pacientes.NEXTVAL, 'Carmen', 'Torres', '89012345-6', TO_DATE('1968-12-05', 'YYYY-MM-DD'), 55, 'F', 'UCI', 'C-02', 'RECUPERACIÓN', 'Post paro cardiorrespiratorio', CURRENT_TIMESTAMP - 8);

-- ============================================================================
-- DATOS DE PRUEBA: SIGNOS VITALES
-- ============================================================================

-- Paciente 1: Juan Pérez (CRÍTICO) - Frecuencia cardíaca alta
INSERT INTO signos_vitales (id, paciente_id, frecuencia_cardiaca, presion_sistolica, presion_diastolica, temperatura, saturacion_oxigeno, frecuencia_respiratoria, estado_conciencia, observaciones, fecha_registro, registrado_por)
VALUES (seq_signos_vitales.NEXTVAL, 1, 125, 150, 95, 37.2, 92, 24, 'VERBAL', 'Paciente agitado, taquicárdico', CURRENT_TIMESTAMP - INTERVAL '2' HOUR, 'Enfermera García');

INSERT INTO signos_vitales (id, paciente_id, frecuencia_cardiaca, presion_sistolica, presion_diastolica, temperatura, saturacion_oxigeno, frecuencia_respiratoria, estado_conciencia, observaciones, fecha_registro, registrado_por)
VALUES (seq_signos_vitales.NEXTVAL, 1, 118, 145, 90, 37.0, 93, 22, 'VERBAL', 'Leve mejoría en FC', CURRENT_TIMESTAMP - INTERVAL '1' HOUR, 'Enfermera García');

-- Paciente 2: María González (MODERADO) - Saturación baja
INSERT INTO signos_vitales (id, paciente_id, frecuencia_cardiaca, presion_sistolica, presion_diastolica, temperatura, saturacion_oxigeno, frecuencia_respiratoria, estado_conciencia, observaciones, fecha_registro, registrado_por)
VALUES (seq_signos_vitales.NEXTVAL, 2, 88, 115, 75, 38.5, 88, 28, 'ALERTA', 'Dificultad respiratoria, requiere O2 suplementario', CURRENT_TIMESTAMP - INTERVAL '3' HOUR, 'Enfermera Morales');

INSERT INTO signos_vitales (id, paciente_id, frecuencia_cardiaca, presion_sistolica, presion_diastolica, temperatura, saturacion_oxigeno, frecuencia_respiratoria, estado_conciencia, observaciones, fecha_registro, registrado_por)
VALUES (seq_signos_vitales.NEXTVAL, 2, 92, 120, 78, 38.2, 91, 26, 'ALERTA', 'Mejora con oxigenoterapia', CURRENT_TIMESTAMP - INTERVAL '1' HOUR, 'Enfermera Morales');

-- Paciente 3: Carlos Rodríguez (CRÍTICO) - Presión baja
INSERT INTO signos_vitales (id, paciente_id, frecuencia_cardiaca, presion_sistolica, presion_diastolica, temperatura, saturacion_oxigeno, frecuencia_respiratoria, estado_conciencia, observaciones, fecha_registro, registrado_por)
VALUES (seq_signos_vitales.NEXTVAL, 3, 110, 75, 45, 36.5, 94, 20, 'DOLOR', 'Hipotensión severa, administrada drogas vasoactivas', CURRENT_TIMESTAMP - INTERVAL '2' HOUR, 'Enfermera Silva');

-- Paciente 4: Ana Martínez (MODERADO) - Fiebre alta
INSERT INTO signos_vitales (id, paciente_id, frecuencia_cardiaca, presion_sistolica, presion_diastolica, temperatura, saturacion_oxigeno, frecuencia_respiratoria, estado_conciencia, observaciones, fecha_registro, registrado_por)
VALUES (seq_signos_vitales.NEXTVAL, 4, 105, 110, 70, 39.2, 95, 22, 'ALERTA', 'Fiebre persistente, hemocultivos tomados', CURRENT_TIMESTAMP - INTERVAL '4' HOUR, 'Enfermera Ramírez');

-- Paciente 5: Pedro López (ESTABLE) - Valores normales
INSERT INTO signos_vitales (id, paciente_id, frecuencia_cardiaca, presion_sistolica, presion_diastolica, temperatura, saturacion_oxigeno, frecuencia_respiratoria, estado_conciencia, observaciones, fecha_registro, registrado_por)
VALUES (seq_signos_vitales.NEXTVAL, 5, 72, 118, 76, 36.8, 98, 16, 'ALERTA', 'Paciente estable, sin novedades', CURRENT_TIMESTAMP - INTERVAL '1' HOUR, 'Enfermera Castro');

-- Paciente 6: Laura Fernández (CRÍTICO) - Saturación crítica
INSERT INTO signos_vitales (id, paciente_id, frecuencia_cardiaca, presion_sistolica, presion_diastolica, temperatura, saturacion_oxigeno, frecuencia_respiratoria, estado_conciencia, observaciones, fecha_registro, registrado_por)
VALUES (seq_signos_vitales.NEXTVAL, 6, 115, 130, 82, 37.5, 85, 32, 'DOLOR', 'Desaturación severa, considerar intubación', CURRENT_TIMESTAMP - INTERVAL '30' MINUTE, 'Enfermera López');

-- Paciente 7: Roberto Silva (MODERADO) - FR elevada
INSERT INTO signos_vitales (id, paciente_id, frecuencia_cardiaca, presion_sistolica, presion_diastolica, temperatura, saturacion_oxigeno, frecuencia_respiratoria, estado_conciencia, observaciones, fecha_registro, registrado_por)
VALUES (seq_signos_vitales.NEXTVAL, 7, 85, 125, 80, 37.0, 92, 26, 'ALERTA', 'Taquipnea, nebulizaciones administradas', CURRENT_TIMESTAMP - INTERVAL '2' HOUR, 'Enfermera Muñoz');

-- Paciente 8: Carmen Torres (RECUPERACIÓN) - Mejorando
INSERT INTO signos_vitales (id, paciente_id, frecuencia_cardiaca, presion_sistolica, presion_diastolica, temperatura, saturacion_oxigeno, frecuencia_respiratoria, estado_conciencia, observaciones, fecha_registro, registrado_por)
VALUES (seq_signos_vitales.NEXTVAL, 8, 78, 122, 78, 36.6, 97, 18, 'ALERTA', 'Paciente en recuperación satisfactoria', CURRENT_TIMESTAMP - INTERVAL '3' HOUR, 'Enfermera Díaz');

-- ============================================================================
-- DATOS DE PRUEBA: ALERTAS
-- ============================================================================

-- Alerta CRÍTICA: Frecuencia cardíaca alta - Paciente 1
INSERT INTO alertas (id, paciente_id, tipo, mensaje, severidad, estado, fecha_creacion)
VALUES (seq_alertas.NEXTVAL, 1, 'FRECUENCIA_CARDIACA_ALTA', 'Frecuencia cardíaca de 125 bpm excede el límite crítico (>120 bpm)', 'CRÍTICA', 'ACTIVA', CURRENT_TIMESTAMP - INTERVAL '2' HOUR);

-- Alerta CRÍTICA: Saturación baja - Paciente 2
INSERT INTO alertas (id, paciente_id, tipo, mensaje, severidad, estado, fecha_creacion)
VALUES (seq_alertas.NEXTVAL, 2, 'SATURACION_OXIGENO_BAJA', 'Saturación de oxígeno de 88% por debajo del límite crítico (<90%)', 'CRÍTICA', 'ACTIVA', CURRENT_TIMESTAMP - INTERVAL '3' HOUR);

-- Alerta CRÍTICA: Presión arterial baja - Paciente 3
INSERT INTO alertas (id, paciente_id, tipo, mensaje, severidad, estado, fecha_creacion)
VALUES (seq_alertas.NEXTVAL, 3, 'PRESION_SISTOLICA_BAJA', 'Presión sistólica de 75 mmHg por debajo del límite crítico (<80 mmHg)', 'CRÍTICA', 'ACTIVA', CURRENT_TIMESTAMP - INTERVAL '2' HOUR);

-- Alerta CRÍTICA: Temperatura alta - Paciente 4
INSERT INTO alertas (id, paciente_id, tipo, mensaje, severidad, estado, fecha_creacion)
VALUES (seq_alertas.NEXTVAL, 4, 'TEMPERATURA_ALTA', 'Temperatura de 39.2°C excede el límite crítico (>38.5°C)', 'CRÍTICA', 'ACTIVA', CURRENT_TIMESTAMP - INTERVAL '4' HOUR);

-- Alerta CRÍTICA: Saturación muy baja - Paciente 6
INSERT INTO alertas (id, paciente_id, tipo, mensaje, severidad, estado, fecha_creacion)
VALUES (seq_alertas.NEXTVAL, 6, 'SATURACION_OXIGENO_CRITICA', 'Saturación de oxígeno de 85% en nivel crítico. Intervención inmediata requerida', 'CRÍTICA', 'ACTIVA', CURRENT_TIMESTAMP - INTERVAL '30' MINUTE);

-- Alerta MODERADA: Frecuencia respiratoria alta - Paciente 6
INSERT INTO alertas (id, paciente_id, tipo, mensaje, severidad, estado, fecha_creacion)
VALUES (seq_alertas.NEXTVAL, 6, 'FRECUENCIA_RESPIRATORIA_ALTA', 'Frecuencia respiratoria de 32 rpm excede el límite moderado (>25 rpm)', 'MODERADA', 'ACTIVA', CURRENT_TIMESTAMP - INTERVAL '30' MINUTE);

-- Alerta MODERADA: Frecuencia respiratoria alta - Paciente 7
INSERT INTO alertas (id, paciente_id, tipo, mensaje, severidad, estado, fecha_creacion)
VALUES (seq_alertas.NEXTVAL, 7, 'FRECUENCIA_RESPIRATORIA_ALTA', 'Frecuencia respiratoria de 26 rpm excede el límite moderado (>25 rpm)', 'MODERADA', 'ACTIVA', CURRENT_TIMESTAMP - INTERVAL '2' HOUR);

-- Alerta RESUELTA: Presión alta - Paciente 1
INSERT INTO alertas (id, paciente_id, tipo, mensaje, severidad, estado, fecha_creacion, fecha_resolucion, resuelto_por, notas_resolucion)
VALUES (seq_alertas.NEXTVAL, 1, 'PRESION_SISTOLICA_ALTA', 'Presión sistólica de 150 mmHg excede el límite crítico (>140 mmHg)', 'CRÍTICA', 'RESUELTA', CURRENT_TIMESTAMP - INTERVAL '5' HOUR, CURRENT_TIMESTAMP - INTERVAL '3' HOUR, 'Dr. Ramírez', 'Administrado antihipertensivo, presión normalizada');

-- Alerta RESUELTA: Saturación baja - Paciente 2
INSERT INTO alertas (id, paciente_id, tipo, mensaje, severidad, estado, fecha_creacion, fecha_resolucion, resuelto_por, notas_resolucion)
VALUES (seq_alertas.NEXTVAL, 2, 'SATURACION_OXIGENO_BAJA', 'Saturación de oxígeno de 88% por debajo del límite moderado (<92%)', 'MODERADA', 'RESUELTA', CURRENT_TIMESTAMP - INTERVAL '6' HOUR, CURRENT_TIMESTAMP - INTERVAL '4' HOUR, 'Dra. Mendoza', 'Oxigenoterapia iniciada, saturación estabilizada en 91%');

-- ============================================================================
-- COMMIT DE TRANSACCIONES
-- ============================================================================

COMMIT;

-- ============================================================================
-- VERIFICACIÓN DE DATOS INSERTADOS
-- ============================================================================

SELECT 'Pacientes insertados: ' || COUNT(*) FROM pacientes;
SELECT 'Signos vitales insertados: ' || COUNT(*) FROM signos_vitales;
SELECT 'Alertas insertadas: ' || COUNT(*) FROM alertas;

-- Mostrar resumen de pacientes por estado
SELECT estado, COUNT(*) as cantidad 
FROM pacientes 
GROUP BY estado 
ORDER BY estado;

-- Mostrar alertas activas por severidad
SELECT severidad, COUNT(*) as cantidad 
FROM alertas 
WHERE estado = 'ACTIVA'
GROUP BY severidad 
ORDER BY severidad;

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================
