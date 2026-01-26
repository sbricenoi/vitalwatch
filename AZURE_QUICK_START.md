# 🔷 VitalWatch en Azure - Quick Start Guide (1 Página)

## ⚡ Despliegue en 3 Pasos

```bash
# 1️⃣ Login en Azure
az login

# 2️⃣ Ejecutar script
./deploy-azure.sh

# 3️⃣ ¡Listo! Abre tu app
# https://vitalwatch-frontend.azurecontainerapps.io
```

**Tiempo:** 1-2 horas | **Costo:** $47-85/mes

---

## 🏗️ Arquitectura

```
INTERNET
    │
    ▼
┌─────────────────── MICROSOFT AZURE ───────────────────┐
│                                                         │
│  Frontend (Angular)                                    │
│       │                                                 │
│       ▼                                                 │
│  API Gateway (Kong)                                    │
│       │                                                 │
│       ▼                                                 │
│  Backend (Spring Boot)                                 │
│       │                                                 │
└───────┼─────────────────────────────────────────────────┘
        │
        ▼ JDBC/TLS
   ┌─────────────┐
   │ ORACLE CLOUD│
   │ Database    │
   └─────────────┘
```

---

## 📋 Servicios Azure Utilizados

| Servicio | Uso | Costo/Mes |
|----------|-----|-----------|
| **Container Apps** | Backend + Frontend + Gateway | $40-75 |
| **Container Registry** | Imágenes Docker | $5 |
| **Key Vault** | Secrets | ~$0 |
| **App Insights** | Monitoreo | $2-5 |

---

## 🔑 URLs Importantes

```
✅ Frontend:     https://vitalwatch-frontend.azurecontainerapps.io
✅ API Gateway:  https://vitalwatch-api-gateway.azurecontainerapps.io
✅ Backend:      https://vitalwatch-backend.azurecontainerapps.io
✅ Swagger:      https://vitalwatch-backend.azurecontainerapps.io/swagger-ui.html
✅ Portal Azure: https://portal.azure.com
```

---

## 🔐 Credenciales de Prueba

```
Admin:      admin@vitalwatch.com      / Admin123!
Médico:     medico@vitalwatch.com     / Medico123!
Enfermera:  enfermera@vitalwatch.com  / Enfermera123!
```

---

## 🛠️ Comandos Esenciales

### Ver Logs
```bash
az containerapp logs show \
  --name vitalwatch-backend \
  --resource-group rg-vitalwatch-prod \
  --follow
```

### Actualizar App
```bash
az containerapp update \
  --name vitalwatch-backend \
  --resource-group rg-vitalwatch-prod \
  --image acrvitalwatch.azurecr.io/vitalwatch-backend:v1.0.1
```

### Escalar
```bash
az containerapp update \
  --name vitalwatch-backend \
  --resource-group rg-vitalwatch-prod \
  --min-replicas 5 --max-replicas 15
```

### Ver Estado
```bash
az containerapp list \
  --resource-group rg-vitalwatch-prod \
  --output table
```

### Eliminar Todo
```bash
./cleanup-azure.sh
```

---

## 🆘 Troubleshooting Rápido

| Problema | Solución |
|----------|----------|
| Container no arranca | `az containerapp logs show --name [APP] --resource-group [RG] --tail 100` |
| Error BD | Verificar wallet: `az containerapp exec --name backend --command "ls /app/wallet"` |
| CORS Error | `az containerapp update --set-env-vars "ALLOWED_ORIGINS=https://..."` |
| 404 Not Found | Verificar ingress: `az containerapp show --query properties.configuration.ingress` |

---

## 📊 Health Checks

```bash
# Backend
curl https://vitalwatch-backend.azurecontainerapps.io/api/v1/health

# Database
curl https://vitalwatch-backend.azurecontainerapps.io/api/v1/health/database

# Frontend
curl https://vitalwatch-frontend.azurecontainerapps.io
```

**Respuesta esperada:** `{"status":"UP"}`

---

## 📚 Documentación Completa

- 📖 [Guía Completa (60 páginas)](docs/GUIA_DESPLIEGUE_AZURE.md)
- 📊 [Resumen Ejecutivo (12 páginas)](docs/AZURE_RESUMEN_EJECUTIVO.md)
- ✅ [Checklist (20 páginas)](docs/AZURE_CHECKLIST.md)
- ⚖️ [Comparación (15 páginas)](docs/AZURE_COMPARACION_OPCIONES.md)
- ⚡ [README Azure (5 páginas)](AZURE_README.md)
- 📚 [Índice Maestro](AZURE_INDEX.md)

---

## 🎯 Próximos Pasos

1. ✅ Desplegar con `./deploy-azure.sh`
2. ✅ Probar la aplicación
3. ✅ Configurar alertas
4. ✅ Configurar CI/CD (opcional)
5. ✅ Custom domain (opcional)

---

## 💰 Optimizar Costos

```bash
# Escalar a 0 cuando no uses (dev)
az containerapp update --min-replicas 0 --max-replicas 0

# Reducir recursos
az containerapp update --cpu 0.5 --memory 1.0Gi

# Ver costos actuales
# Portal Azure → Cost Management → Cost Analysis
```

---

## ⚠️ Importante

- ✅ Wallet de Oracle incluido en imagen Docker
- ✅ HTTPS automático (Let's Encrypt)
- ✅ Auto-scaling habilitado
- ✅ Zero-downtime deployments
- ✅ Multi-region disponible
- ⚠️ Monitorear costos diariamente
- ⚠️ Hacer backup de configuración
- ⚠️ Usar `cleanup-azure.sh` al finalizar proyecto

---

**🚀 ¡Listo para desplegar!**

```bash
./deploy-azure.sh
```

---

**Fecha:** 2026-01-26 | **Versión:** 1.0.0 | **Proyecto:** VitalWatch
