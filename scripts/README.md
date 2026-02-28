# 🛠️ Scripts de Automatización

## 📋 Índice de Scripts

### Kafka

| Script | Descripción | Uso |
|--------|-------------|-----|
| `quick-start-kafka.sh` | ⭐ Inicio completo automatizado | `./quick-start-kafka.sh` |
| `start-kafka-cluster.sh` | Solo cluster (Zookeeper + Kafka) | `./start-kafka-cluster.sh` |
| `create-kafka-topics.sh` | Crear topics Kafka | `./create-kafka-topics.sh` |

### Deploy

| Script | Descripción | Uso |
|--------|-------------|-----|
| `deploy-kafka-azure-rapido.sh` | ⭐ Deploy Kafka a Azure | `./deploy-kafka-azure-rapido.sh` |
| `deploy-kafka-azure.sh` | Deploy Kafka completo | `./deploy-kafka-azure.sh` |
| `deploy-azure.sh` | Deploy RabbitMQ a Azure | `./deploy-azure.sh` |
| `deploy-rabbitmq-azure.sh` | Deploy alternativo | `./deploy-rabbitmq-azure.sh` |

### Utilidades

| Script | Descripción | Uso |
|--------|-------------|-----|
| `cleanup-azure.sh` | Limpiar recursos Azure | `./cleanup-azure.sh` |
| `setup-git.sh` | Configurar Git | `./setup-git.sh` |

---

## 🚀 Uso Recomendado

### Para Desarrollo Local (Kafka)

```bash
cd scripts/
./quick-start-kafka.sh
```

Esto inicia TODO automáticamente:
- Zookeepers (espera 30s)
- Kafka Brokers (espera 45s)
- Kafka UI (espera 10s)
- Crea topics
- Inicia 4 microservicios
- Verifica health

### Para Deploy a Azure

```bash
cd scripts/

# Editar variables primero
nano deploy-kafka-azure-rapido.sh

# Ejecutar
./deploy-kafka-azure-rapido.sh
```

---

## ⚙️ Permisos

Todos los scripts tienen permisos de ejecución:

```bash
chmod +x *.sh
```

---

## 🔗 Ver Más

- **Arquitectura:** `../docs/ARQUITECTURA.md`
- **Deploy:** `../docs/GUIA_DEPLOY.md`
- **Uso:** `../docs/GUIA_USO.md`
