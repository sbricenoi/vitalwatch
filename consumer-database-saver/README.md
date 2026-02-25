# 💾 VitalWatch - Consumidor 1: Database Saver

Microservicio consumidor que guarda signos vitales y alertas en Oracle Cloud Database.

## 🎯 Funcionalidad

- Consume mensajes de 2 tópicos:
  - `signos-vitales-stream`: Stream continuo de mediciones
  - `alertas-medicas`: Alertas detectadas
- Guarda en Oracle Cloud Autonomous Database
- Usa Spring Data JPA con Hibernate
- Procesamiento concurrente (3 listeners para vital signs, 2 para alerts)

## 📦 Tablas en Oracle

### SIGNOS_VITALES_KAFKA
Almacena cada medición del stream:
- Metadatos de Kafka (topic, partition, offset)
- Datos del paciente
- Signos vitales completos
- Timestamp de medición

### ALERTAS_KAFKA
Almacena alertas detectadas:
- Metadatos de Kafka
- Detalles de la alerta
- Anomalías en formato JSON
- Estado de la alerta

## 📊 Rendimiento

- **Mensajes por segundo:** ~60 (signos vitales)
- **Alertas por minuto:** ~9 (15% de anomalías)
- **Concurrencia:** 3 listeners para vital signs, 2 para alertas
- **Batch size:** 100 mensajes por poll

## 🔧 Configuración

Requiere Oracle Wallet en `/app/wallet/` con:
- cwallet.sso
- ewallet.p12
- tnsnames.ora
- sqlnet.ora

## 🏃 Ejecución

### Local
```bash
export ORACLE_WALLET_PATH=/path/to/wallet
mvn spring-boot:run
```

### Docker
```bash
docker build -t consumer-database-saver .
docker run -v ./Wallet_S58ONUXCX4C1QXE9:/app/wallet:ro consumer-database-saver
```

## 📈 Logs

```
💾 Signos vitales guardados en Oracle: 60 registros | Último: Juan Pérez - FC: 75
🚨 Alerta guardada en Oracle: ALERT-xxx | Paciente: María García | Severidad: CRITICA | Total: 9
```
