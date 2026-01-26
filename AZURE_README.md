# 🔷 VitalWatch en Azure - Guía Rápida

## 🚀 Despliegue Rápido (Automatizado)

```bash
# 1. Ejecutar script de despliegue automatizado
./deploy-azure.sh
```

El script te guiará paso a paso y desplegará toda la infraestructura automáticamente.

---

## 📝 Despliegue Manual (Paso a Paso)

Si prefieres tener más control, consulta la guía completa:

```bash
# Ver guía detallada
cat docs/GUIA_DESPLIEGUE_AZURE.md
```

---

## ⚡ Comandos Rápidos

### Ver Estado de los Servicios

```bash
# Cargar configuración
source azure-config.env

# Ver todos los recursos
az resource list --resource-group $RESOURCE_GROUP --output table

# Ver Container Apps
az containerapp list \
  --resource-group $RESOURCE_GROUP \
  --output table

# Ver logs del Backend
az containerapp logs show \
  --name vitalwatch-backend \
  --resource-group $RESOURCE_GROUP \
  --tail 50 \
  --follow

# Ver logs del Frontend
az containerapp logs show \
  --name vitalwatch-frontend \
  --resource-group $RESOURCE_GROUP \
  --tail 50 \
  --follow

# Ver logs del API Gateway
az containerapp logs show \
  --name vitalwatch-api-gateway \
  --resource-group $RESOURCE_GROUP \
  --tail 50 \
  --follow
```

### Health Checks

```bash
# Backend
curl https://vitalwatch-backend.azurecontainerapps.io/api/v1/health

# API Gateway
curl https://vitalwatch-api-gateway.azurecontainerapps.io/api/v1/health

# Frontend
curl https://vitalwatch-frontend.azurecontainerapps.io
```

### Actualizar una Aplicación

```bash
# Cargar configuración
source azure-config.env

# 1. Reconstruir imagen
cd backend
docker build -t vitalwatch-backend:v1.0.1 .

# 2. Tag y push a ACR
ACR_LOGIN_SERVER=$(az acr show --name $ACR_NAME --query loginServer --output tsv)
docker tag vitalwatch-backend:v1.0.1 $ACR_LOGIN_SERVER/vitalwatch-backend:v1.0.1
docker push $ACR_LOGIN_SERVER/vitalwatch-backend:v1.0.1

# 3. Actualizar Container App (zero-downtime)
az containerapp update \
  --name vitalwatch-backend \
  --resource-group $RESOURCE_GROUP \
  --image $ACR_LOGIN_SERVER/vitalwatch-backend:v1.0.1

cd ..
```

### Escalar Servicios

```bash
# Escalar Backend
az containerapp update \
  --name vitalwatch-backend \
  --resource-group $RESOURCE_GROUP \
  --min-replicas 3 \
  --max-replicas 15

# Escalar Frontend
az containerapp update \
  --name vitalwatch-frontend \
  --resource-group $RESOURCE_GROUP \
  --min-replicas 2 \
  --max-replicas 20
```

### Ver Métricas

```bash
# CPU y Memoria del Backend
az monitor metrics list \
  --resource $(az containerapp show --name vitalwatch-backend --resource-group $RESOURCE_GROUP --query id --output tsv) \
  --metric "UsageNanoCores" "WorkingSetBytes" \
  --output table

# Requests del Backend
az monitor metrics list \
  --resource $(az containerapp show --name vitalwatch-backend --resource-group $RESOURCE_GROUP --query id --output tsv) \
  --metric "Requests" \
  --output table
```

### Rollback

```bash
# Listar revisiones disponibles
az containerapp revision list \
  --name vitalwatch-backend \
  --resource-group $RESOURCE_GROUP \
  --output table

# Activar revisión anterior
az containerapp revision activate \
  --name vitalwatch-backend \
  --resource-group $RESOURCE_GROUP \
  --revision [NOMBRE_REVISION_ANTERIOR]
```

---

## 🛑 Detener/Eliminar Recursos

### Detener Servicios (sin eliminar)

```bash
# Escalar a 0 para detener sin eliminar
az containerapp update \
  --name vitalwatch-backend \
  --resource-group $RESOURCE_GROUP \
  --min-replicas 0 \
  --max-replicas 0

az containerapp update \
  --name vitalwatch-frontend \
  --resource-group $RESOURCE_GROUP \
  --min-replicas 0 \
  --max-replicas 0

az containerapp update \
  --name vitalwatch-api-gateway \
  --resource-group $RESOURCE_GROUP \
  --min-replicas 0 \
  --max-replicas 0
```

### Reiniciar Servicios

```bash
# Restaurar escalado
az containerapp update \
  --name vitalwatch-backend \
  --resource-group $RESOURCE_GROUP \
  --min-replicas 2 \
  --max-replicas 10

az containerapp update \
  --name vitalwatch-frontend \
  --resource-group $RESOURCE_GROUP \
  --min-replicas 1 \
  --max-replicas 10

az containerapp update \
  --name vitalwatch-api-gateway \
  --resource-group $RESOURCE_GROUP \
  --min-replicas 2 \
  --max-replicas 5
```

### Eliminar TODO (⚠️ CUIDADO)

```bash
# Usar el script de limpieza
./cleanup-azure.sh

# O manualmente:
source azure-config.env
az group delete --name $RESOURCE_GROUP --yes --no-wait
```

---

## 💰 Estimación de Costos

| Servicio | Configuración | Costo Aprox/Mes |
|----------|---------------|-----------------|
| Container Apps (Backend) | 2-10 replicas, 1 vCPU, 2GB | ~$20-40 |
| Container Apps (Frontend) | 1-10 replicas, 0.5 vCPU, 1GB | ~$10-20 |
| Container Apps (Gateway) | 2-5 replicas, 0.5 vCPU, 1GB | ~$10-15 |
| Azure Container Registry | Basic SKU | ~$5 |
| Azure Key Vault | Secrets storage | ~$0.03 |
| Azure Monitor | Logs & Metrics | ~$2-5 |
| **TOTAL ESTIMADO** | | **~$47-85/mes** |

**Nota**: Costos reales pueden variar según uso. Revisa el [Azure Pricing Calculator](https://azure.microsoft.com/pricing/calculator/).

### Reducir Costos

```bash
# 1. Usar SKU más económico
az containerapp update \
  --name vitalwatch-backend \
  --resource-group $RESOURCE_GROUP \
  --cpu 0.5 \
  --memory 1.0Gi

# 2. Reducir número de réplicas mínimas
az containerapp update \
  --name vitalwatch-backend \
  --resource-group $RESOURCE_GROUP \
  --min-replicas 1 \
  --max-replicas 5

# 3. Detener servicios en horarios no productivos (automatizar con Azure Automation)
```

---

## 🔧 Troubleshooting

### Container no arranca

```bash
# Ver logs detallados
az containerapp logs show \
  --name vitalwatch-backend \
  --resource-group $RESOURCE_GROUP \
  --tail 100

# Ver estado de la última revisión
az containerapp revision list \
  --name vitalwatch-backend \
  --resource-group $RESOURCE_GROUP \
  --output table

# Reiniciar container
az containerapp revision restart \
  --name vitalwatch-backend \
  --resource-group $RESOURCE_GROUP
```

### Error de conexión a Oracle DB

```bash
# Verificar variables de entorno
az containerapp show \
  --name vitalwatch-backend \
  --resource-group $RESOURCE_GROUP \
  --query properties.template.containers[0].env

# Ejecutar shell en el container
az containerapp exec \
  --name vitalwatch-backend \
  --resource-group $RESOURCE_GROUP \
  --command "/bin/sh"

# Dentro del container, verificar wallet:
ls -la /app/wallet
```

### CORS Errors

```bash
# Verificar configuración de CORS
az containerapp show \
  --name vitalwatch-backend \
  --resource-group $RESOURCE_GROUP \
  --query properties.template.containers[0].env

# Actualizar CORS
az containerapp update \
  --name vitalwatch-backend \
  --resource-group $RESOURCE_GROUP \
  --set-env-vars "ALLOWED_ORIGINS=https://tu-frontend-url.azurecontainerapps.io"
```

### Alto uso de recursos

```bash
# Ver métricas actuales
az monitor metrics list \
  --resource $(az containerapp show --name vitalwatch-backend --resource-group $RESOURCE_GROUP --query id --output tsv) \
  --metric "UsageNanoCores" "WorkingSetBytes" \
  --output table

# Aumentar recursos
az containerapp update \
  --name vitalwatch-backend \
  --resource-group $RESOURCE_GROUP \
  --cpu 2.0 \
  --memory 4.0Gi
```

---

## 📊 Monitoreo

### Portal de Azure

1. Ir a: https://portal.azure.com
2. Buscar Resource Group: `rg-vitalwatch-prod`
3. Ver:
   - Container Apps → Logs, Metrics, Revisions
   - Application Insights → Performance, Failures, Users
   - Container Registry → Repositories, Access keys

### Azure CLI Dashboard

```bash
# Crear archivo de monitoreo
cat > monitor.sh << 'EOF'
#!/bin/bash
source azure-config.env

watch -n 5 '
echo "=== VitalWatch Status ==="
echo ""
echo "Container Apps:"
az containerapp list --resource-group $RESOURCE_GROUP --query "[].{Name:name, Status:properties.provisioningState, URL:properties.configuration.ingress.fqdn}" -o table
echo ""
echo "Latest Revisions:"
az containerapp revision list --name vitalwatch-backend --resource-group $RESOURCE_GROUP --query "[0].{Name:name, Active:properties.active, Created:properties.createdTime}" -o table
'
EOF

chmod +x monitor.sh
./monitor.sh
```

---

## 🔐 Seguridad

### Rotar Credenciales de ACR

```bash
az acr credential renew \
  --name $ACR_NAME \
  --password-name password

# Obtener nueva contraseña
az acr credential show --name $ACR_NAME
```

### Actualizar Secrets en Key Vault

```bash
# Actualizar password de Oracle
az keyvault secret set \
  --vault-name $KEYVAULT_NAME \
  --name "oracle-password" \
  --value "NUEVA_PASSWORD"

# Reiniciar backend para aplicar cambios
az containerapp revision restart \
  --name vitalwatch-backend \
  --resource-group $RESOURCE_GROUP
```

### Habilitar Firewall en ACR

```bash
# Permitir solo tu IP
MY_IP=$(curl -s ifconfig.me)
az acr network-rule add \
  --name $ACR_NAME \
  --ip-address $MY_IP
```

---

## 📚 Recursos Adicionales

- [Documentación Completa](docs/GUIA_DESPLIEGUE_AZURE.md)
- [Azure Container Apps Docs](https://learn.microsoft.com/azure/container-apps/)
- [Azure CLI Reference](https://learn.microsoft.com/cli/azure/)
- [Arquitectura del Sistema](docs/arquitectura.md)

---

## 🆘 Soporte

Si encuentras problemas:

1. Revisa los logs: `az containerapp logs show --name [APP_NAME] --resource-group $RESOURCE_GROUP --follow`
2. Consulta la [Guía de Troubleshooting](docs/GUIA_DESPLIEGUE_AZURE.md#troubleshooting)
3. Verifica el estado de Azure: https://status.azure.com/
4. Revisa la documentación oficial de Azure

---

**Última actualización**: 2026-01-26  
**Versión**: 1.0.0
