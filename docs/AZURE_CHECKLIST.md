# ✅ Checklist de Despliegue Azure - VitalWatch

## Pre-Despliegue

### Cuenta y Configuración Azure

- [ ] Cuenta de Azure creada y activa
- [ ] Subscripción con créditos disponibles (estudiante o pay-as-you-go)
- [ ] Azure CLI instalado (`az --version`)
- [ ] Autenticado en Azure CLI (`az login`)
- [ ] Subscripción correcta seleccionada (`az account show`)

### Herramientas Locales

- [ ] Docker Desktop instalado y corriendo
- [ ] Git instalado y configurado
- [ ] Terminal/Shell disponible (Bash, Zsh, PowerShell)
- [ ] Navegador web actualizado
- [ ] Editor de texto o IDE (VSCode recomendado)

### Proyecto Local

- [ ] Código fuente clonado/descargado
- [ ] Oracle Wallet presente (`Wallet_S58ONUXCX4C1QXE9/`)
- [ ] Dockerfiles validados (`backend/Dockerfile`, `frontend/Dockerfile`)
- [ ] Backend construye correctamente (`cd backend && mvn clean package`)
- [ ] Frontend construye correctamente (`cd frontend && npm install`)
- [ ] Docker compose funciona localmente (`docker-compose up`)

---

## Fase 1: Preparación (15 min)

### Configuración Inicial

- [ ] Crear archivo `azure-config.env` con variables de entorno
- [ ] Definir nombre único para Container Registry (solo letras y números)
- [ ] Definir región de Azure más cercana
- [ ] Revisar y ajustar nombres de recursos si es necesario

### Decisiones de Arquitectura

- [ ] **Opción A**: Usar script automatizado (`./deploy-azure.sh`) ⭐ Recomendado
- [ ] **Opción B**: Despliegue manual paso a paso (más control)
- [ ] Decidir si usar Kong o Azure API Management
- [ ] Decidir si usar custom domain (opcional)
- [ ] Decidir si usar Azure Front Door para CDN (opcional)

---

## Fase 2: Azure Container Registry (20 min)

### Crear ACR

- [ ] Resource Group creado
- [ ] Azure Container Registry creado
- [ ] Admin habilitado en ACR
- [ ] Login exitoso en ACR (`az acr login`)
- [ ] Credenciales de ACR guardadas

### Construir y Publicar Imágenes

#### Backend
- [ ] Imagen de backend construida localmente
- [ ] Imagen tagueada para ACR
- [ ] Imagen de backend publicada en ACR
- [ ] Verificar imagen en ACR (`az acr repository show-tags`)

#### API Gateway (Kong)
- [ ] Dockerfile creado para Kong
- [ ] Configuración `kong.yml` lista
- [ ] Imagen de Kong construida
- [ ] Imagen de Kong publicada en ACR
- [ ] Verificar imagen en ACR

#### Frontend
- [ ] Configuración de producción actualizada
- [ ] Imagen de frontend construida
- [ ] Imagen de frontend publicada en ACR
- [ ] Verificar imagen en ACR

### Validación ACR

- [ ] Todas las imágenes listadas correctamente
- [ ] Tags correctos aplicados (latest, vX.X.X)
- [ ] Tamaños de imágenes razonables
- [ ] ACR accesible desde Azure CLI

---

## Fase 3: Secrets y Configuración (10 min)

### Azure Key Vault

- [ ] Key Vault creado con nombre único
- [ ] Secret `oracle-username` agregado
- [ ] Secret `oracle-password` agregado
- [ ] Secret `oracle-service` agregado
- [ ] Verificar acceso a secrets

### Oracle Wallet

- [ ] **Opción A**: Wallet empaquetado en imagen Docker ⭐ Más simple
- [ ] **Opción B**: Wallet subido a Azure File Share (más seguro)
- [ ] Verificar que wallet es accesible desde container

---

## Fase 4: Container Apps Environment (15 min)

### Crear Environment

- [ ] Extensión de Container Apps instalada
- [ ] Providers de Azure registrados
- [ ] Container Apps Environment creado
- [ ] Verificar estado del environment
- [ ] Log Analytics Workspace creado (automático)

---

## Fase 5: Despliegue del Backend (20 min)

### Crear Container App Backend

- [ ] Container App del backend creado
- [ ] Imagen correcta especificada
- [ ] Variables de entorno configuradas:
  - [ ] `ORACLE_USERNAME`
  - [ ] `ORACLE_PASSWORD`
  - [ ] `TNS_ADMIN`
  - [ ] `SERVER_PORT`
  - [ ] `SPRING_APPLICATION_NAME`
- [ ] Ingress externo habilitado
- [ ] Puerto 8080 configurado
- [ ] Scaling configurado (min: 2, max: 10)
- [ ] Recursos asignados (1 CPU, 2GB RAM)

### Validar Backend

- [ ] URL del backend obtenida
- [ ] Health check responde: `curl https://[BACKEND_URL]/api/v1/health`
- [ ] Database health check: `curl https://[BACKEND_URL]/api/v1/health/database`
- [ ] Logs sin errores críticos
- [ ] Conexión a Oracle DB exitosa

---

## Fase 6: Despliegue del API Gateway (15 min)

### Actualizar Configuración Kong

- [ ] `kong.yml` actualizado con URL del backend de Azure
- [ ] CORS configurado para Azure URLs
- [ ] Rate limiting configurado
- [ ] Security headers configurados
- [ ] Imagen reconstruida y publicada

### Crear Container App Gateway

- [ ] Container App del gateway creado
- [ ] Imagen correcta especificada
- [ ] Variables de entorno Kong configuradas
- [ ] Ingress externo habilitado
- [ ] Puerto 8000 configurado
- [ ] Scaling configurado (min: 2, max: 5)
- [ ] Recursos asignados (0.5 CPU, 1GB RAM)

### Validar Gateway

- [ ] URL del gateway obtenida
- [ ] Health check a través del gateway: `curl https://[GATEWAY_URL]/api/v1/health`
- [ ] CORS funcionando correctamente
- [ ] Rate limiting aplicado
- [ ] Logs sin errores

---

## Fase 7: Despliegue del Frontend (20 min)

### Actualizar Configuración Frontend

- [ ] `environment.prod.ts` actualizado con URL del gateway
- [ ] Configuración de API URL correcta
- [ ] Imagen reconstruida con nueva configuración
- [ ] Imagen publicada en ACR

### Crear Container App Frontend

- [ ] Container App del frontend creado
- [ ] Imagen correcta especificada
- [ ] Variable `API_URL` configurada
- [ ] Ingress externo habilitado
- [ ] Puerto 80 configurado
- [ ] Scaling configurado (min: 1, max: 10)
- [ ] Recursos asignados (0.5 CPU, 1GB RAM)

### Validar Frontend

- [ ] URL del frontend obtenida
- [ ] Frontend carga en el navegador
- [ ] Assets estáticos cargan correctamente
- [ ] No hay errores en la consola del navegador
- [ ] Página de login accesible

---

## Fase 8: Configuración de Networking (10 min)

### CORS

- [ ] Backend actualizado con URLs de frontend y gateway
- [ ] CORS headers presentes en responses
- [ ] Preflight requests funcionando

### Comunicación entre Servicios

- [ ] Frontend puede comunicarse con Gateway
- [ ] Gateway puede comunicarse con Backend
- [ ] Backend puede comunicarse con Oracle DB
- [ ] No hay errores de conexión en logs

---

## Fase 9: Pruebas Funcionales (30 min)

### Pruebas de API

- [ ] Health check backend: ✅
- [ ] Health check database: ✅
- [ ] Login endpoint funciona
- [ ] Listar pacientes funciona
- [ ] Crear paciente funciona
- [ ] Registrar signos vitales funciona
- [ ] Alertas se generan automáticamente
- [ ] Dashboard muestra estadísticas

### Pruebas de Frontend

- [ ] Login con credenciales de prueba:
  - [ ] Admin: `admin@vitalwatch.com` / `Admin123!`
  - [ ] Médico: `medico@vitalwatch.com` / `Medico123!`
  - [ ] Enfermera: `enfermera@vitalwatch.com` / `Enfermera123!`
- [ ] Dashboard carga correctamente
- [ ] Módulo de Pacientes:
  - [ ] Listar pacientes
  - [ ] Crear nuevo paciente
  - [ ] Editar paciente
  - [ ] Ver detalle paciente
- [ ] Módulo de Signos Vitales:
  - [ ] Registrar signos vitales
  - [ ] Ver historial
  - [ ] Validación de formularios
- [ ] Módulo de Alertas:
  - [ ] Ver alertas activas
  - [ ] Ver alertas críticas
  - [ ] Resolver alerta
  - [ ] Descartar alerta

### Pruebas de Seguridad

- [ ] Autenticación requerida para rutas protegidas
- [ ] JWT token funciona correctamente
- [ ] Logout funciona
- [ ] CORS solo permite orígenes configurados
- [ ] Rate limiting funciona
- [ ] HTTPS habilitado en todos los servicios

### Pruebas de Rendimiento (Opcional)

- [ ] Test de carga básico con Apache Bench
- [ ] Auto-scaling funciona bajo carga
- [ ] Tiempos de respuesta aceptables (< 1s)
- [ ] Sin memory leaks detectados

---

## Fase 10: Monitoreo y Observabilidad (15 min)

### Application Insights

- [ ] Application Insights creado
- [ ] Instrumentation key obtenido
- [ ] Backend conectado a App Insights
- [ ] Frontend conectado a App Insights
- [ ] Métricas visibles en portal

### Logs

- [ ] Logs de backend accesibles
- [ ] Logs de frontend accesibles
- [ ] Logs de gateway accesibles
- [ ] Formato de logs legible
- [ ] Nivel de logging apropiado

### Alertas

- [ ] Alerta de CPU alta configurada
- [ ] Alerta de errores HTTP configurada
- [ ] Alerta de memoria alta configurada (opcional)
- [ ] Notificaciones configuradas (email/SMS)

### Métricas

- [ ] CPU usage monitoreable
- [ ] Memory usage monitoreable
- [ ] Request rate monitoreable
- [ ] Response time monitoreable
- [ ] Error rate monitoreable

---

## Fase 11: Documentación y Entrega (20 min)

### Documentación Actualizada

- [ ] URLs de producción documentadas
- [ ] Credenciales guardadas de forma segura
- [ ] Diagrama de arquitectura actualizado
- [ ] README actualizado con URLs de Azure
- [ ] Procedimientos de deploy documentados

### Entregables

- [ ] `azure-deployment-urls.txt` generado
- [ ] Capturas de pantalla del portal Azure
- [ ] Capturas de pantalla de la aplicación funcionando
- [ ] Logs de despliegue guardados
- [ ] Evidencia de pruebas funcionales

### Backup

- [ ] Configuración de Azure exportada
- [ ] Scripts de deploy guardados
- [ ] Variables de entorno documentadas
- [ ] Secrets anotados (en lugar seguro)

---

## Post-Despliegue

### Optimización

- [ ] Revisar costos en Azure Cost Management
- [ ] Ajustar scaling si es necesario
- [ ] Optimizar tamaño de imágenes Docker
- [ ] Configurar cache si aplica
- [ ] Implementar CDN si es necesario (Azure Front Door)

### Seguridad

- [ ] Rotar credenciales de ACR
- [ ] Revisar permisos de Key Vault
- [ ] Configurar firewall en ACR (opcional)
- [ ] Habilitar Azure Security Center (opcional)
- [ ] Configurar backup de Key Vault

### CI/CD (Opcional pero Recomendado)

- [ ] Configurar GitHub Actions para deploy automático
- [ ] Pipeline de CI para tests
- [ ] Pipeline de CD para deploy
- [ ] Environments separados (dev, staging, prod)

### Mantenimiento

- [ ] Crear runbook para actualizaciones
- [ ] Documentar procedimiento de rollback
- [ ] Configurar mantenimiento programado
- [ ] Plan de disaster recovery
- [ ] Documentar contactos de soporte

---

## Validación Final

### Checklist de Go-Live

- [ ] ✅ Todos los servicios arrancados y saludables
- [ ] ✅ Todas las pruebas funcionales pasadas
- [ ] ✅ Monitoreo configurado y funcionando
- [ ] ✅ Logs accesibles y legibles
- [ ] ✅ Alertas configuradas
- [ ] ✅ Documentación completa
- [ ] ✅ Backup de configuración realizado
- [ ] ✅ Equipo entrenado en operaciones básicas
- [ ] ✅ Procedimientos de emergencia documentados
- [ ] ✅ Costos dentro del presupuesto

### Información de Contacto

- [ ] Azure Support contactado (si es necesario)
- [ ] Contactos del equipo documentados
- [ ] On-call rotation definida (si aplica)

---

## URLs y Recursos Finales

Completar al finalizar el despliegue:

```
✅ URLs de Producción
├── Frontend:        https://___________________________
├── API Gateway:     https://___________________________
└── Backend:         https://___________________________

✅ Recursos Azure
├── Resource Group:  ___________________________
├── ACR:             ___________________________
├── Key Vault:       ___________________________
└── App Insights:    ___________________________

✅ Credenciales de Prueba
├── Admin:           admin@vitalwatch.com / Admin123!
├── Médico:          medico@vitalwatch.com / Medico123!
└── Enfermera:       enfermera@vitalwatch.com / Enfermera123!
```

---

## Notas Importantes

### 🔴 No Olvidar

1. **Costos**: Monitorear costos diariamente los primeros días
2. **Logs**: Revisar logs frecuentemente por errores
3. **Scaling**: Ajustar min/max replicas según carga real
4. **Backup**: Guardar configuración localmente
5. **Limpieza**: Usar `./cleanup-azure.sh` cuando termine el proyecto

### 📝 Lecciones Aprendidas

Espacio para documentar problemas encontrados y soluciones:

```
Problema 1: ________________________________________________
Solución:   ________________________________________________

Problema 2: ________________________________________________
Solución:   ________________________________________________

Problema 3: ________________________________________________
Solución:   ________________________________________________
```

---

**Tiempo Total Estimado**: 3-4 horas

**Fecha de Despliegue**: _______________

**Completado por**: _______________

**Revisado por**: _______________

---

**✅ Checklist Completado: [ ] SÍ [ ] NO**
