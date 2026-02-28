#!/bin/bash

# =============================================================================
# VitalWatch - Script de Creación de Tópicos Kafka
# =============================================================================

set -e

echo "📊 VitalWatch - Creando Tópicos Kafka"
echo "====================================="
echo ""

KAFKA_CONTAINER="vitalwatch-kafka1"
BOOTSTRAP_SERVER="kafka1:9092"

echo "🔍 Verificando que Kafka esté corriendo..."
if ! docker ps | grep -q "$KAFKA_CONTAINER"; then
    echo "❌ Error: Contenedor $KAFKA_CONTAINER no está corriendo"
    echo "   Ejecuta primero: ./start-kafka-cluster.sh"
    exit 1
fi

echo "✅ Kafka está corriendo"
echo ""

echo "📝 Creando tópico: signos-vitales-stream"
echo "   - Particiones: 3"
echo "   - Factor de replicación: 2"
echo "   - Retención: 7 días"
echo ""

docker exec -it $KAFKA_CONTAINER kafka-topics \
    --bootstrap-server $BOOTSTRAP_SERVER \
    --create \
    --topic signos-vitales-stream \
    --partitions 3 \
    --replication-factor 2 \
    --config retention.ms=604800000 \
    --config compression.type=snappy \
    --if-not-exists

echo ""
echo "📝 Creando tópico: alertas-medicas"
echo "   - Particiones: 3"
echo "   - Factor de replicación: 2"
echo "   - Retención: 30 días"
echo ""

docker exec -it $KAFKA_CONTAINER kafka-topics \
    --bootstrap-server $BOOTSTRAP_SERVER \
    --create \
    --topic alertas-medicas \
    --partitions 3 \
    --replication-factor 2 \
    --config retention.ms=2592000000 \
    --config compression.type=snappy \
    --if-not-exists

echo ""
echo "✅ Tópicos creados exitosamente"
echo ""

echo "📋 Listando todos los tópicos:"
docker exec -it $KAFKA_CONTAINER kafka-topics \
    --bootstrap-server $BOOTSTRAP_SERVER \
    --list

echo ""
echo "🔍 Descripción del tópico signos-vitales-stream:"
docker exec -it $KAFKA_CONTAINER kafka-topics \
    --bootstrap-server $BOOTSTRAP_SERVER \
    --describe \
    --topic signos-vitales-stream

echo ""
echo "🔍 Descripción del tópico alertas-medicas:"
docker exec -it $KAFKA_CONTAINER kafka-topics \
    --bootstrap-server $BOOTSTRAP_SERVER \
    --describe \
    --topic alertas-medicas

echo ""
echo "✅ Configuración completa"
echo ""
echo "📝 Siguiente paso: Iniciar microservicios"
echo "   docker-compose -f docker-compose-kafka.yml up -d"
echo ""
