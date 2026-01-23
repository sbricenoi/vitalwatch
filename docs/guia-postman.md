# 📮 Guía de Uso - Colección Postman VitalWatch

## 🎯 Descripción

La colección de Postman incluye **34 endpoints completamente documentados** para probar toda la API de VitalWatch.

---

## 📦 ¿Qué incluye la colección?

### 1. Health Check (1 endpoint)
- Verificación del estado del servidor

### 2. Pacientes (7 endpoints)
- Listar todos los pacientes
- Obtener paciente por ID
- Crear nuevo paciente
- Actualizar paciente
- Eliminar paciente
- Buscar por estado
- Buscar por sala

### 3. Signos Vitales (7 endpoints)
- Obtener historial por paciente
- Obtener último registro
- Obtener por ID
- Registrar signos vitales normales
- Registrar signos vitales anormales (genera alertas)
- Actualizar registro
- Eliminar registro

### 4. Alertas (9 endpoints)
- Listar todas
- Listar activas
- Listar críticas
- Obtener por paciente
- Obtener por ID
- Crear alerta manual
- Resolver alerta
- Descartar alerta
- Eliminar alerta

### 5. Dashboard & Estadísticas (3 endpoints)
- Obtener estadísticas generales
- Resumen por estado
- Alertas recientes

---

## 🚀 Configuración Inicial

### Paso 1: Importar la Colección

```bash
1. Abrir Postman
2. Click en "Import"
3. Seleccionar "File"
4. Navegar a: docs/postman-collection.json
5. Click en "Import"
```

### Paso 2: Configurar Variables de Entorno

La colección incluye variables pre-configuradas que debes ajustar:

| Variable | Valor Inicial | Descripción |
|----------|---------------|-------------|
| `base_url` | `http://localhost:8080` | URL del backend (cambiar en producción) |
| `api_version` | `v1` | Versión de la API |
| `jwt_token` | `YOUR_JWT_TOKEN_HERE` | Token JWT de Auth0/Keycloak |

**Para editar variables:**
1. Click derecho en la colección "VitalWatch"
2. Edit → Variables
3. Actualizar los valores
4. Save

### Paso 3: Obtener Token JWT

#### Opción A: Auth0

```bash
# Hacer request POST a Auth0
POST https://YOUR_DOMAIN.auth0.com/oauth/token
Content-Type: application/json

{
  "client_id": "YOUR_CLIENT_ID",
  "client_secret": "YOUR_CLIENT_SECRET",
  "audience": "YOUR_API_IDENTIFIER",
  "grant_type": "client_credentials"
}

# Copiar el "access_token" de la respuesta
```

#### Opción B: Keycloak

```bash
# Hacer request POST a Keycloak
POST http://localhost:8180/realms/vitalwatch/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password
&username=tu_usuario
&password=tu_password
&client_id=vitalwatch-client

# Copiar el "access_token" de la respuesta
```

#### Guardar el Token

1. Click derecho en la colección "VitalWatch"
2. Edit → Variables
3. Encontrar `jwt_token`
4. Pegar el token en "Current value"
5. Save

---

## 🧪 Probando los Endpoints

### Test 1: Health Check (Sin autenticación)

```
GET {{base_url}}/actuator/health
```

**Respuesta esperada:**
```json
{
  "status": "UP"
}
```

### Test 2: Listar Pacientes (Con autenticación)

```
GET {{base_url}}/api/{{api_version}}/pacientes
Authorization: Bearer {{jwt_token}}
```

**Respuesta esperada:**
```json
{
  "traceId": "uuid-generado",
  "code": "200",
  "message": "Pacientes obtenidos exitosamente",
  "data": [
    {
      "id": 1,
      "nombre": "Juan",
      "apellido": "Pérez",
      "rut": "12345678-9",
      "edad": 65,
      "estado": "CRÍTICO",
      "sala": "UCI",
      "cama": "A-01"
    }
  ]
}
```

### Test 3: Crear Paciente

```
POST {{base_url}}/api/{{api_version}}/pacientes
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
  "nombre": "María",
  "apellido": "González",
  "rut": "98765432-1",
  "fechaNacimiento": "1975-08-22",
  "edad": 48,
  "genero": "F",
  "sala": "UCI",
  "cama": "B-02",
  "estado": "MODERADO",
  "diagnostico": "Neumonía bilateral"
}
```

### Test 4: Registrar Signos Vitales (Genera Alerta)

```
POST {{base_url}}/api/{{api_version}}/signos-vitales
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
  "pacienteId": 1,
  "frecuenciaCardiaca": 125,
  "presionSistolica": 165,
  "presionDiastolica": 95,
  "temperatura": 38.9,
  "saturacionOxigeno": 88,
  "frecuenciaRespiratoria": 28,
  "estadoConciencia": "VERBAL",
  "observaciones": "Paciente agitado",
  "registradoPor": "Enfermera López"
}
```

**Nota:** Este registro generará múltiples alertas automáticamente porque los valores están fuera de rango.

### Test 5: Ver Alertas Activas

```
GET {{base_url}}/api/{{api_version}}/alertas/activas
Authorization: Bearer {{jwt_token}}
```

### Test 6: Resolver Alerta

```
PUT {{base_url}}/api/{{api_version}}/alertas/1/resolver
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
  "resueltoPor": "Dr. Ramírez",
  "notasResolucion": "Medicación administrada. Paciente estabilizado."
}
```

---

## ✅ Tests Automáticos Incluidos

Cada endpoint incluye tests automáticos que validan:

1. **Status Code**: Verifica que sea 200 o 201
2. **traceId**: Verifica que exista en la respuesta
3. **code**: Verifica que exista
4. **message**: Verifica que exista
5. **data**: Verifica que exista

Para ver los resultados:
- Ejecutar cualquier request
- Click en la pestaña "Test Results"
- Verificar que todos los tests pasen ✅

---

## 🔄 Flujo Completo de Prueba

### Escenario: Paciente con Crisis

```
1. Crear paciente
   POST /api/v1/pacientes

2. Registrar signos vitales anormales
   POST /api/v1/signos-vitales
   (Genera alertas automáticamente)

3. Ver alertas del paciente
   GET /api/v1/alertas/paciente/{id}

4. Ver alertas críticas
   GET /api/v1/alertas/criticas

5. Resolver alerta
   PUT /api/v1/alertas/{id}/resolver

6. Verificar estadísticas
   GET /api/v1/dashboard/estadisticas
```

---

## 📊 Variables de Referencia - Signos Vitales

### Valores Normales

| Signo Vital | Rango Normal |
|-------------|--------------|
| Frecuencia Cardíaca | 60-100 bpm |
| Presión Sistólica | 90-140 mmHg |
| Presión Diastólica | 60-90 mmHg |
| Temperatura | 36-37.5 °C |
| Saturación O2 | 95-100 % |
| Frecuencia Resp. | 12-20 rpm |

### Valores que Generan Alertas Críticas

| Signo Vital | Alerta Crítica |
|-------------|----------------|
| Frecuencia Cardíaca | >120 o <50 bpm |
| Presión Sistólica | >160 o <80 mmHg |
| Presión Diastólica | >100 o <50 mmHg |
| Temperatura | >38.5 o <35 °C |
| Saturación O2 | <90 % |
| Frecuencia Resp. | >25 o <10 rpm |

---

## 🐛 Troubleshooting

### Error: "Unauthorized" (401)

**Problema:** Token JWT no válido o expirado

**Solución:**
1. Obtener nuevo token de Auth0/Keycloak
2. Actualizar variable `jwt_token`
3. Verificar que el token no haya expirado

### Error: "Forbidden" (403)

**Problema:** Usuario no tiene permisos para el endpoint

**Solución:**
- Verificar roles en Auth0/Keycloak
- Asegurarse de tener rol correcto (ADMIN/DOCTOR/ENFERMERA)

### Error: "Connection Refused"

**Problema:** Backend no está ejecutándose

**Solución:**
```bash
cd backend
./mvnw spring-boot:run
```

### Error: "Database Connection Failed"

**Problema:** No se puede conectar a Oracle Cloud

**Solución:**
1. Verificar que el Wallet esté en la ubicación correcta
2. Verificar credenciales en application.properties
3. Verificar conectividad de red

---

## 💡 Tips Útiles

### 1. Usar Entornos

Crea diferentes entornos para desarrollo, staging y producción:

```
Postman → Environments → Create Environment

Desarrollo:
- base_url: http://localhost:8080
- jwt_token: [token_dev]

Producción:
- base_url: https://api.vitalwatch.com
- jwt_token: [token_prod]
```

### 2. Exportar Resultados

```
Postman → Runner → Select Collection → Run
Export Results → JSON
```

### 3. Compartir Colección

```
Click derecho en colección → Share
Generate Link → Copy
```

### 4. Documentación Automática

```
Postman → View in Web → Publish Documentation
```

---

## 📚 Recursos Adicionales

- **Documentación Swagger:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs
- **Health Check:** http://localhost:8080/actuator/health

---

## ✅ Checklist de Pruebas

Antes de entregar el proyecto, verifica que funcionen:

- [ ] Health check (sin auth)
- [ ] Listar pacientes
- [ ] Crear paciente con validaciones
- [ ] Actualizar paciente
- [ ] Buscar pacientes por estado/sala
- [ ] Registrar signos vitales normales
- [ ] Registrar signos vitales anormales (genera alertas)
- [ ] Ver alertas activas
- [ ] Ver alertas críticas
- [ ] Resolver alerta
- [ ] Dashboard estadísticas
- [ ] Todos los tests automáticos pasan ✅

---

## 🎥 Para el Video de Presentación

**Demostración sugerida con Postman:**

1. **Health Check** (15 seg)
   - Mostrar que el servidor está funcionando

2. **Crear Paciente** (30 seg)
   - POST con datos completos
   - Mostrar respuesta con traceId

3. **Registrar Signos Vitales Críticos** (45 seg)
   - POST con valores fuera de rango
   - Explicar que genera alertas automáticamente

4. **Ver Alertas Generadas** (30 seg)
   - GET alertas activas
   - Mostrar alertas críticas

5. **Resolver Alerta** (30 seg)
   - PUT resolver con notas
   - Verificar que cambió a RESUELTA

6. **Dashboard** (15 seg)
   - GET estadísticas
   - Mostrar resumen del sistema

**Total: ~3 minutos de demostración en Postman**

---

**Última actualización:** 2026-01-22
**Versión:** 1.0
**Total de endpoints:** 34
