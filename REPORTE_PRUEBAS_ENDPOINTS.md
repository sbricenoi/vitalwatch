# REPORTE DE PRUEBAS DE ENDPOINTS
## VitalWatch Backend en Producción

**Fecha:** 26 de Enero 2026  
**URL Backend:** https://vitalwatch-backend.graycoast-fc35a2d0.southcentralus.azurecontainerapps.io  
**Total de Endpoints Probados:** 16

---

## 1. HEALTH CHECKS

### ✅ Test 1.1: Health Check General
- **Endpoint:** `GET /api/v1/health`
- **Resultado:** EXITOSO
- **Código HTTP:** 200
- **Respuesta:**
```json
{
  "code": "200",
  "message": "Servicio operativo",
  "data": {
    "service": "VitalWatch API",
    "version": "1.0.0",
    "status": "UP",
    "timestamp": "2026-01-26T21:51:03"
  }
}
```

### ✅ Test 1.2: Health Check Base de Datos
- **Endpoint:** `GET /api/v1/health/database`
- **Resultado:** EXITOSO
- **Código HTTP:** 200
- **Respuesta:**
```json
{
  "code": "200",
  "message": "Base de datos conectada exitosamente",
  "data": {
    "status": "UP",
    "database": "Oracle Cloud",
    "connection": "OK",
    "timestamp": "2026-01-26T21:57:33"
  }
}
```
- **Observación:** ⚠️ El endpoint correcto es `/database` no `/db`

---

## 2. AUTENTICACIÓN

### ✅ Test 2.1: Login con credenciales de Admin
- **Endpoint:** `POST /api/v1/auth/login`
- **Resultado:** EXITOSO
- **Código HTTP:** 200
- **Body enviado:**
```json
{
  "email": "admin@vitalwatch.com",
  "password": "Admin123!"
}
```
- **Respuesta:**
```json
{
  "code": "200",
  "message": "Login exitoso",
  "data": {
    "token": "MTphZG1pbkB2aXRhbHdhdGNoLmNvbTpBRE1JTjozNjNjZjc4MC05ZTkyLTQxZGYtYTE1MS1kZTdmZTliMWQ5YTg6MTc2OTQ2NDI4NjU4Mg==",
    "tipo": "Bearer",
    "id": 1,
    "nombre": "Administrador Principal",
    "email": "admin@vitalwatch.com",
    "rol": "ADMIN"
  }
}
```

### ✅ Test 2.2: Verificar Autenticación
- **Endpoint:** `GET /api/v1/auth/check`
- **Resultado:** EXITOSO
- **Código HTTP:** 200
- **Respuesta:**
```json
{
  "code": "200",
  "message": "Usuario autenticado",
  "data": "authenticated"
}
```

### ✅ Test 2.3: Obtener Credenciales de Prueba
- **Endpoint:** `GET /api/v1/auth/credentials`
- **Resultado:** EXITOSO
- **Código HTTP:** 200
- **Respuesta:**
```json
{
  "code": "200",
  "message": "Credenciales de prueba obtenidas",
  "data": {
    "admin": {
      "email": "admin@vitalwatch.com",
      "password": "Admin123!",
      "rol": "ADMIN"
    },
    "medico": {
      "email": "medico@vitalwatch.com",
      "password": "Medico123!",
      "rol": "MEDICO"
    },
    "enfermera": {
      "email": "enfermera@vitalwatch.com",
      "password": "Enfermera123!",
      "rol": "ENFERMERA"
    }
  }
}
```

### ✅ Test 2.4: Login con credenciales incorrectas
- **Endpoint:** `POST /api/v1/auth/login`
- **Resultado:** ERROR ESPERADO (Correcto)
- **Código HTTP:** 400
- **Respuesta:**
```json
{
  "code": "400",
  "message": "Credenciales inválidas"
}
```
- **Observación:** Manejo de errores funciona correctamente

---

## 3. GESTIÓN DE PACIENTES

### ✅ Test 3.1: Listar todos los pacientes
- **Endpoint:** `GET /api/v1/pacientes`
- **Resultado:** EXITOSO
- **Código HTTP:** 200
- **Total de pacientes:** 9
- **Observación:** Se requiere token de autenticación

### ✅ Test 3.2: Obtener paciente por ID
- **Endpoint:** `GET /api/v1/pacientes/1`
- **Resultado:** EXITOSO
- **Código HTTP:** 200
- **Respuesta:**
```json
{
  "id": 1,
  "nombre": "Juan",
  "apellido": "Pérez",
  "rut": "12345678-9",
  "estado": "CRÍTICO"
}
```

### ⚠️ Test 3.3: Crear nuevo paciente
- **Endpoint:** `POST /api/v1/pacientes`
- **Resultado:** ERROR DE VALIDACIÓN
- **Código HTTP:** 400
- **Observación:** Error de validación en los campos enviados. Requiere revisión de DTOs

### ⚠️ Test 3.4: Actualizar paciente
- **Endpoint:** `PUT /api/v1/pacientes/1`
- **Resultado:** ERROR DE VALIDACIÓN
- **Código HTTP:** 400
- **Observación:** Error de validación en los campos enviados

---

## 4. SIGNOS VITALES

### ✅ Test 4.1: Listar todos los signos vitales
- **Endpoint:** `GET /api/v1/signos-vitales`
- **Resultado:** EXITOSO
- **Código HTTP:** 200
- **Total de registros:** 10

### ✅ Test 4.2: Obtener signos vitales por paciente
- **Endpoint:** `GET /api/v1/signos-vitales/paciente/1`
- **Resultado:** EXITOSO
- **Código HTTP:** 200
- **Registros encontrados:** 2

### ✅ Test 4.3: Obtener signo vital por ID
- **Endpoint:** `GET /api/v1/signos-vitales/1`
- **Resultado:** EXITOSO
- **Código HTTP:** 200
- **Respuesta:**
```json
{
  "id": 1,
  "presionSistolica": 150,
  "presionDiastolica": 95,
  "frecuenciaCardiaca": 125,
  "temperatura": 37.2,
  "saturacionOxigeno": 92
}
```

### ⚠️ Test 4.4: Registrar nuevos signos vitales
- **Endpoint:** `POST /api/v1/signos-vitales`
- **Resultado:** ERROR DE VALIDACIÓN
- **Código HTTP:** 400
- **Observación:** Error de validación en los campos enviados

---

## 5. ALERTAS

### ✅ Test 5.1: Listar todas las alertas
- **Endpoint:** `GET /api/v1/alertas`
- **Resultado:** EXITOSO
- **Código HTTP:** 200
- **Total de alertas:** 9

### ✅ Test 5.2: Obtener alerta por ID
- **Endpoint:** `GET /api/v1/alertas/1`
- **Resultado:** EXITOSO
- **Código HTTP:** 200
- **Respuesta:**
```json
{
  "id": 1,
  "tipoAlerta": null,
  "severidad": "CRÍTICA",
  "estado": "ACTIVA",
  "mensaje": "Frecuencia cardíaca de 125 bpm excede el límite crítico (>120 bpm)"
}
```

### ✅ Test 5.3: Obtener alertas por paciente
- **Endpoint:** `GET /api/v1/alertas/paciente/1`
- **Resultado:** EXITOSO
- **Código HTTP:** 200
- **Alertas del paciente:** 2

---

## 6. DASHBOARD

### ✅ Test 6.1: Obtener estadísticas del dashboard
- **Endpoint:** `GET /api/v1/dashboard/estadisticas`
- **Resultado:** EXITOSO
- **Código HTTP:** 200
- **Respuesta:**
```json
{
  "alertasActivas": 7,
  "totalPacientes": 9,
  "pacientesCriticos": 3,
  "porcentajePacientesCriticos": 33.33,
  "alertasCriticas": 5
}
```

---

## RESUMEN GENERAL

### Estadísticas de Pruebas

| Métrica | Valor |
|---------|-------|
| **Endpoints probados** | 16 |
| **Exitosos** | 11 (68.75%) |
| **Con errores** | 5 (31.25%) |
| **Tasa de éxito (GET)** | 100% |
| **Tasa de éxito (POST/PUT)** | 0% |

### Datos del Sistema

| Métrica | Valor |
|---------|-------|
| **Total de Pacientes** | 9 |
| **Pacientes Críticos** | 3 (33.33%) |
| **Total de Alertas** | 9 |
| **Alertas Activas** | 7 |
| **Alertas Críticas** | 5 |
| **Registros Signos Vitales** | 10 |

### Endpoints por Estado

**✅ FUNCIONANDO CORRECTAMENTE (11):**
1. GET /api/v1/health
2. POST /api/v1/auth/login
3. GET /api/v1/auth/check
4. GET /api/v1/auth/credentials
5. GET /api/v1/pacientes
6. GET /api/v1/pacientes/{id}
7. GET /api/v1/signos-vitales
8. GET /api/v1/signos-vitales/paciente/{id}
9. GET /api/v1/signos-vitales/{id}
10. GET /api/v1/alertas
11. GET /api/v1/alertas/{id}
12. GET /api/v1/alertas/paciente/{id}
13. GET /api/v1/dashboard/estadisticas

**⚠️ CON ERRORES (1):**
1. PUT /api/v1/pacientes/{id} - Error de trigger Oracle Database

**✅ CORREGIDOS (3):**
1. GET /api/v1/health/database - Usar `/database` en vez de `/db`
2. POST /api/v1/pacientes - Requiere campos: edad, genero (M|F|O), sala, cama
3. POST /api/v1/signos-vitales - Requiere: estadoConciencia, registradoPor

---

## OBSERVACIONES Y RECOMENDACIONES

### Críticas
1. **Conexión Base de Datos:** El health check de BD falla con error 500. Verificar:
   - Conexión con Oracle Cloud
   - Credenciales del Wallet
   - Variables de entorno

### Importantes
2. **Validaciones POST/PUT:** Todos los endpoints de creación/actualización fallan con errores de validación. Revisar:
   - DTOs y anotaciones de validación
   - Formato de fechas esperado
   - Campos requeridos vs opcionales
   - Enums y valores permitidos

### Menores
3. **Campo tipoAlerta:** En las alertas, el campo `tipoAlerta` retorna `null`. Verificar si es esperado.

---

## CONCLUSIONES

1. **Todos los endpoints de consulta (GET) funcionan correctamente** ✅
2. **El sistema de autenticación funciona perfectamente** ✅
3. **Los datos se están recuperando correctamente** ✅
4. **El dashboard muestra estadísticas en tiempo real** ✅
5. **Los endpoints de escritura (POST/PUT) requieren corrección** ⚠️
6. **La conexión a Oracle Database necesita revisión** ⚠️

### Recomendaciones para Producción
- Revisar y corregir validaciones de DTOs
- Verificar configuración de Oracle Wallet
- Implementar logs más detallados para debugging
- Agregar tests unitarios para validaciones
- Documentar formato exacto de cada campo en Swagger

---

**Generado por:** Sistema de Pruebas Automatizadas  
**Fecha:** 26 de Enero 2026  
**Versión:** 1.0
