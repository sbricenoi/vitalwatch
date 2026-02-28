# 🚀 Guía de Deploy - VitalWatch

## 📋 Prerrequisitos

### Software Requerido
- Docker 20.10+
- Docker Compose 2.0+
- Git
- Azure CLI (para deploy cloud)

### Configuración Necesaria
- Oracle Cloud Database
- Oracle Wallet descargado
- Credenciales de acceso

---

## 🏠 Deploy Local

### Opción 1: Sistema RabbitMQ

#### Paso 1: Clonar y Configurar

```bash
# Clonar
git clone https://github.com/sbricenoi/vitalwatch.git
cd vitalwatch

# Cambiar a rama RabbitMQ
git checkout feature/rabbitmq-integration

# Copiar Oracle Wallet
cp -r /ruta/al/Wallet_* ./Wallet_S58ONUXCX4C1QXE9
```

#### Paso 2: Configurar Variables

Editar `docker-compose.yml` y ajustar:

```yaml
environment:
  ORACLE_DB_URL: "tu_jdbc_url"
  ORACLE_DB_USERNAME: "ADMIN"
  ORACLE_DB_PASSWORD: "tu_password"
```

#### Paso 3: Iniciar Sistema

```bash
# Crear tablas en Oracle primero
# Conectar a SQL Developer y ejecutar: database/schema.sql

# Iniciar contenedores
docker-compose up -d

# Verificar
docker-compose ps
```

#### Paso 4: Acceder

- **Frontend:** http://localhost
- **Backend:** http://localhost:8080
- **RabbitMQ UI:** http://localhost:15672 (guest/guest)
- **API Gateway:** http://localhost:8000

---

### Opción 2: Sistema Kafka

#### Paso 1: Preparar

```bash
# Cambiar a rama Kafka
git checkout feature/kafka-implementation

# Copiar Oracle Wallet a consumers
cp -r Wallet_S58ONUXCX4C1QXE9 consumer-database-saver/wallet/
cp -r Wallet_S58ONUXCX4C1QXE9 consumer-summary-generator/wallet/
```

#### Paso 2: Crear Tablas Oracle

```sql
-- Conectar a SQL Developer
-- Ejecutar: database/create_tables_kafka.sql
```

#### Paso 3: Inicio Automático

```bash
# Script todo-en-uno
chmod +x quick-start-kafka.sh
./quick-start-kafka.sh

# Esperará ~3 minutos y:
# - Inicia Zookeepers
# - Inicia Kafka Brokers
# - Inicia Kafka UI
# - Crea tópicos
# - Inicia microservicios
# - Verifica health
```

#### Paso 4: Acceder

- **Kafka UI:** http://localhost:9000
- **Stream Generator:** http://localhost:8091
- **Alert Processor:** http://localhost:8092
- **DB Saver:** http://localhost:8093
- **Summary Generator:** http://localhost:8094

---

## ☁️ Deploy Azure

### Prerrequisitos Azure

```bash
# Instalar Azure CLI
# macOS
brew install azure-cli

# Login
az login

# Verificar suscripción
az account show
```

### Deploy RabbitMQ a Azure

#### Opción A: Script Automatizado

```bash
# Revisar y editar variables
nano deploy-azure.sh

# Variables a configurar:
# - RESOURCE_GROUP
# - LOCATION  
# - ACR_NAME
# - ORACLE_DB_PASSWORD

# Ejecutar
chmod +x deploy-azure.sh
./deploy-azure.sh
```

#### Opción B: Manual

```bash
# 1. Crear Resource Group
az group create \
  --name rg-vitalwatch-prod \
  --location southcentralus

# 2. Crear ACR
az acr create \
  --resource-group rg-vitalwatch-prod \
  --name acrvitalwatch \
  --sku Basic

# 3. Login a ACR
az acr login --name acrvitalwatch

# 4. Build y Push
docker build -t acrvitalwatch.azurecr.io/backend:latest ./backend
docker push acrvitalwatch.azurecr.io/backend:latest

# Repetir para todos los servicios

# 5. Crear Container Apps Environment
az containerapp env create \
  --name env-vitalwatch-prod \
  --resource-group rg-vitalwatch-prod \
  --location southcentralus

# 6. Deploy cada microservicio
az containerapp create \
  --name vitalwatch-backend \
  --resource-group rg-vitalwatch-prod \
  --environment env-vitalwatch-prod \
  --image acrvitalwatch.azurecr.io/backend:latest \
  --target-port 8080 \
  --ingress external \
  --env-vars ORACLE_DB_URL="..." \
  --cpu 0.5 \
  --memory 1.0Gi
```

---

### Deploy Kafka a Azure

#### Script Automatizado

```bash
# Usar infraestructura existente
chmod +x deploy-kafka-azure-rapido.sh

# Editar password de Oracle
nano deploy-kafka-azure-rapido.sh
# Buscar: ORACLE_PASSWORD

# Ejecutar (~20 minutos)
./deploy-kafka-azure-rapido.sh
```

#### Qué hace el script:

1. ✅ Usa ACR existente
2. ✅ Crea Azure Event Hubs (Kafka-compatible)
3. ✅ Build y push de 4 imágenes
4. ✅ Deploy de 4 microservicios
5. ✅ Configura variables de entorno
6. ✅ Obtiene URLs de acceso

#### Verificar Deploy

```bash
# Ver recursos
az resource list \
  --resource-group rg-vitalwatch-prod \
  --output table

# Ver URLs
az containerapp show \
  --name vitalwatch-kafka-stream \
  --resource-group rg-vitalwatch-prod \
  --query properties.configuration.ingress.fqdn \
  --output tsv

# Ver logs
az containerapp logs show \
  --name vitalwatch-kafka-stream \
  --resource-group rg-vitalwatch-prod \
  --follow
```

---

## 🔧 Configuración Avanzada

### Health Checks

Todos los microservicios exponen:
```
/actuator/health
/actuator/info
/actuator/metrics
```

### Variables de Entorno

#### Backend
```env
SPRING_PROFILES_ACTIVE=docker
ORACLE_DB_URL=jdbc:oracle:thin:@...
ORACLE_DB_USERNAME=ADMIN
ORACLE_DB_PASSWORD=password
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
```

#### Kafka Services
```env
SPRING_PROFILES_ACTIVE=docker
KAFKA_BOOTSTRAP_SERVERS=kafka1:9092,kafka2:9092,kafka3:9092
ORACLE_DB_URL=jdbc:oracle:thin:@...
ORACLE_DB_USERNAME=ADMIN
ORACLE_DB_PASSWORD=password
```

### Escalado

#### Docker Compose Local
```bash
# Escalar un servicio
docker-compose up -d --scale producer-anomaly-detector=3
```

#### Azure Container Apps
```bash
# Configurar auto-scaling
az containerapp update \
  --name vitalwatch-backend \
  --resource-group rg-vitalwatch-prod \
  --min-replicas 1 \
  --max-replicas 5 \
  --scale-rule-name http-rule \
  --scale-rule-type http \
  --scale-rule-http-concurrency 50
```

---

## 🐛 Troubleshooting

### Problema: Contenedor no inicia

```bash
# Ver logs
docker logs container-name

# Ver últimas 100 líneas
docker logs --tail 100 container-name

# Seguir logs en tiempo real
docker logs -f container-name
```

### Problema: No conecta a Oracle

```bash
# Verificar wallet
ls -la consumer-database-saver/wallet/

# Debe contener:
# - tnsnames.ora
# - sqlnet.ora
# - cwallet.sso
# - ewallet.p12

# Test conexión
docker exec container-name \
  java -jar app.jar --spring.datasource.url="..."
```

### Problema: RabbitMQ no responde

```bash
# Verificar estado
docker-compose ps rabbitmq

# Reiniciar
docker-compose restart rabbitmq

# Ver management UI
open http://localhost:15672
```

### Problema: Kafka LAG alto

```bash
# Ver consumer groups
docker exec vitalwatch-kafka1 kafka-consumer-groups \
  --bootstrap-server kafka1:9092 \
  --list

# Ver detalles de lag
docker exec vitalwatch-kafka1 kafka-consumer-groups \
  --bootstrap-server kafka1:9092 \
  --group alert-processor-group \
  --describe
```

---

## 🧹 Limpieza

### Local

```bash
# Detener todos los contenedores
docker-compose down

# Detener y eliminar volúmenes
docker-compose down -v

# Limpiar todo Docker
docker system prune -a --volumes
```

### Azure

```bash
# Eliminar Resource Group completo
az group delete \
  --name rg-vitalwatch-prod \
  --yes \
  --no-wait

# Verificar eliminación
az group list --output table
```

---

## 📊 Monitoreo Post-Deploy

### Métricas Clave

```bash
# CPU y Memoria
docker stats

# Logs agregados
docker-compose logs -f

# Health checks
watch -n 5 'curl -s http://localhost:8080/actuator/health'
```

### Azure Monitoring

```bash
# Ver métricas
az monitor metrics list \
  --resource vitalwatch-backend \
  --resource-group rg-vitalwatch-prod \
  --resource-type Microsoft.App/containerApps

# Configurar alertas
az monitor metrics alert create \
  --name high-cpu-alert \
  --resource-group rg-vitalwatch-prod \
  --condition "avg Percentage CPU > 80"
```

---

## ✅ Checklist de Deploy

### Pre-Deploy
- [ ] Oracle Database creado y accesible
- [ ] Wallet descargado
- [ ] Tablas creadas (ejecutar SQLs)
- [ ] Docker instalado y corriendo
- [ ] Variables de entorno configuradas

### Deploy Local
- [ ] `git clone` ejecutado
- [ ] Rama correcta (`git checkout`)
- [ ] Wallet copiado a ubicación correcta
- [ ] `docker-compose up -d` exitoso
- [ ] Health checks passing
- [ ] Interfaces accesibles

### Deploy Azure
- [ ] Azure CLI instalado
- [ ] Login exitoso (`az login`)
- [ ] Suscripción activa
- [ ] Script de deploy configurado
- [ ] Deploy ejecutado sin errores
- [ ] URLs obtenidas
- [ ] Health checks passing

### Post-Deploy
- [ ] Monitoreo configurado
- [ ] Logs verificados
- [ ] Tests ejecutados
- [ ] Documentación actualizada
- [ ] Equipo notificado

---

## 🔗 Referencias

- **Docker Compose:** https://docs.docker.com/compose/
- **Azure Container Apps:** https://learn.microsoft.com/azure/container-apps/
- **Azure CLI:** https://learn.microsoft.com/cli/azure/
- **Oracle Cloud:** https://docs.oracle.com/en/cloud/
