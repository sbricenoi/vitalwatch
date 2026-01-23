# 🔐 Guía de Configuración - Oracle Cloud Database

## 📊 Información de la Base de Datos

**Base de Datos:** Oracle Autonomous Database
- **Nombre:** s58onuxcx4c1qxe9
- **Tipo:** Autonomous Database
- **Región:** Santiago, Chile (sa-santiago-1)
- **Host:** adb.sa-santiago-1.oraclecloud.com
- **Puerto:** 1522 (TCPS - SSL/TLS)

---

## 📁 Wallet de Oracle Cloud

El Wallet ya está incluido en el proyecto:

```
Wallet_S58ONUXCX4C1QXE9/
├── cwallet.sso          # Wallet seguro Oracle
├── keystore.jks         # Keystore Java
├── ojdbc.properties     # Propiedades JDBC
├── README               # Información del Wallet
├── sqlnet.ora           # Configuración SQL*Net
├── tnsnames.ora         # Servicios disponibles
└── truststore.jks       # Truststore Java
```

**⚠️ IMPORTANTE:** NO subir el Wallet a repositorios públicos. Ya está en `.gitignore`.

---

## 🔌 Servicios de Conexión Disponibles

El archivo `tnsnames.ora` define 5 servicios con diferentes niveles de rendimiento:

| Servicio | Uso Recomendado | Descripción |
|----------|-----------------|-------------|
| `s58onuxcx4c1qxe9_high` | Producción | Alto rendimiento, máxima concurrencia |
| `s58onuxcx4c1qxe9_medium` | Staging | Rendimiento medio |
| `s58onuxcx4c1qxe9_low` | Desarrollo | Bajo consumo, ideal para pruebas |
| `s58onuxcx4c1qxe9_tp` | Transaction Processing | Optimizado para transacciones |
| `s58onuxcx4c1qxe9_tpurgent` | Crítico | Transacciones urgentes, máxima prioridad |

**Recomendación:**
- **Desarrollo:** Usa `_low`
- **Producción:** Usa `_high`

---

## ⚙️ Configuración en Spring Boot

### application.properties

```properties
# Conexión usando Wallet (servicio HIGH para producción)
spring.datasource.url=jdbc:oracle:thin:@s58onuxcx4c1qxe9_high?TNS_ADMIN=./Wallet_S58ONUXCX4C1QXE9
spring.datasource.username=ADMIN
spring.datasource.password=TU_PASSWORD_AQUI

# Driver Oracle
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# Dialect para Oracle
spring.jpa.database-platform=org.hibernate.dialect.OracleDialect
```

### application-dev.properties (Desarrollo)

```properties
# Usar servicio LOW en desarrollo para ahorrar recursos
spring.datasource.url=jdbc:oracle:thin:@s58onuxcx4c1qxe9_low?TNS_ADMIN=./Wallet_S58ONUXCX4C1QXE9
spring.datasource.username=ADMIN
spring.datasource.password=TU_PASSWORD_AQUI
```

### application-prod.properties (Producción)

```properties
# Usar servicio HIGH en producción
spring.datasource.url=jdbc:oracle:thin:@s58onuxcx4c1qxe9_high?TNS_ADMIN=/app/wallet
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

---

## 🐳 Configuración en Docker

### Dockerfile del Backend

```dockerfile
FROM openjdk:17-jdk-slim

# Copiar Wallet
COPY Wallet_S58ONUXCX4C1QXE9 /app/wallet

# Copiar JAR
COPY target/vitalwatch-backend.jar /app/app.jar

WORKDIR /app

# Configurar TNS_ADMIN
ENV TNS_ADMIN=/app/wallet

CMD ["java", "-jar", "app.jar"]
```

### docker-compose.yml

```yaml
backend:
  build: ./backend
  volumes:
    - ./Wallet_S58ONUXCX4C1QXE9:/app/wallet:ro
  environment:
    - SPRING_DATASOURCE_URL=jdbc:oracle:thin:@s58onuxcx4c1qxe9_high?TNS_ADMIN=/app/wallet
    - SPRING_DATASOURCE_USERNAME=ADMIN
    - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
```

**Nota:** El `:ro` en volumes significa "read-only" (solo lectura) para mayor seguridad.

---

## 🔧 Herramientas de Conexión

### 1. SQL Developer

**Configuración:**
1. Abrir Oracle SQL Developer
2. Click en "+" para nueva conexión
3. Configurar:
   - **Name:** VitalWatch Oracle Cloud
   - **Username:** ADMIN
   - **Password:** [tu password]
   - **Connection Type:** Cloud Wallet
   - **Configuration File:** Seleccionar `Wallet_S58ONUXCX4C1QXE9/`
   - **Service:** s58onuxcx4c1qxe9_low (o el que prefieras)
4. Test → Connect

### 2. SQLcl (Command Line)

```bash
# Instalar SQLcl (si no lo tienes)
brew install sqlcl  # macOS
# o descargar de: https://www.oracle.com/tools/downloads/sqlcl-downloads.html

# Configurar TNS_ADMIN
export TNS_ADMIN=/path/to/Wallet_S58ONUXCX4C1QXE9

# Conectarse
sql ADMIN/[password]@s58onuxcx4c1qxe9_low

# Ejecutar scripts
SQL> @database/schema.sql
SQL> @database/data.sql
```

### 3. SQL*Plus

```bash
# Configurar TNS_ADMIN
export TNS_ADMIN=/path/to/Wallet_S58ONUXCX4C1QXE9

# Conectarse
sqlplus ADMIN/[password]@s58onuxcx4c1qxe9_low

# Ejecutar scripts
SQL> @database/schema.sql
SQL> @database/data.sql
```

### 4. DBeaver

**Configuración:**
1. Nueva conexión → Oracle
2. Connection Type: TNS
3. Database: s58onuxcx4c1qxe9_low
4. TNS Names: Seleccionar `Wallet_S58ONUXCX4C1QXE9/tnsnames.ora`
5. Username: ADMIN
6. Password: [tu password]
7. SSL: Configurar Keystore y Truststore del Wallet

---

## 📝 Ejecutar Scripts SQL

### Opción 1: SQL Developer (GUI)

```
1. Conectarse a la BD
2. File → Open → Seleccionar schema.sql
3. Ejecutar (F5 o botón Run)
4. Repetir con data.sql
```

### Opción 2: SQLcl (Command Line)

```bash
# Conectarse
sql ADMIN/[password]@s58onuxcx4c1qxe9_low

# Ejecutar scripts
@database/schema.sql
@database/data.sql

# Verificar
SELECT 'Pacientes: ' || COUNT(*) FROM pacientes;
SELECT 'Signos Vitales: ' || COUNT(*) FROM signos_vitales;
SELECT 'Alertas: ' || COUNT(*) FROM alertas;
```

### Opción 3: Script Automatizado

Crear archivo `setup-database.sh`:

```bash
#!/bin/bash

echo "Configurando base de datos VitalWatch..."

export TNS_ADMIN=./Wallet_S58ONUXCX4C1QXE9

sql ADMIN/[password]@s58onuxcx4c1qxe9_low <<EOF
@database/schema.sql
@database/data.sql

SELECT 'Configuración completada' AS status FROM dual;
SELECT 'Pacientes: ' || COUNT(*) AS count FROM pacientes;
SELECT 'Signos Vitales: ' || COUNT(*) AS count FROM signos_vitales;
SELECT 'Alertas: ' || COUNT(*) AS count FROM alertas;

exit;
EOF

echo "¡Base de datos configurada exitosamente!"
```

---

## ✅ Verificar Conexión

### Test desde Spring Boot

```java
@SpringBootTest
class DatabaseConnectionTest {
    
    @Autowired
    private DataSource dataSource;
    
    @Test
    void testDatabaseConnection() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            assertTrue(conn.isValid(5));
            System.out.println("✅ Conexión exitosa a Oracle Cloud!");
        }
    }
}
```

### Test desde línea de comandos

```bash
# Con SQLcl
sql ADMIN/[password]@s58onuxcx4c1qxe9_low <<< "SELECT 'Conexión OK' FROM dual;"

# Resultado esperado:
# Conexión OK
# ----------
# Conexión OK
```

---

## 🔐 Gestión de Credenciales

### ❌ NO HACER:

```properties
# NO hardcodear password en código
spring.datasource.password=MiPasswordSecreto123
```

### ✅ HACER:

**Opción 1: Variables de Entorno**

```properties
spring.datasource.password=${DB_PASSWORD}
```

```bash
# En tu shell
export DB_PASSWORD="tu_password_secreto"

# Ejecutar aplicación
./mvnw spring-boot:run
```

**Opción 2: Archivo .env (no subir a Git)**

```bash
# .env
DB_PASSWORD=tu_password_secreto
```

```properties
# application.properties
spring.datasource.password=${DB_PASSWORD}
```

**Opción 3: Spring Cloud Config**

Para producción, usar servidor de configuración centralizado.

---

## 🐛 Troubleshooting

### Error: "IO Error: The Network Adapter could not establish the connection"

**Causa:** No encuentra el Wallet

**Solución:**
```bash
# Verificar que TNS_ADMIN apunte correctamente
echo $TNS_ADMIN
ls -la $TNS_ADMIN

# Verificar URL en application.properties
spring.datasource.url=jdbc:oracle:thin:@s58onuxcx4c1qxe9_high?TNS_ADMIN=./Wallet_S58ONUXCX4C1QXE9
```

### Error: "ORA-01017: invalid username/password"

**Causa:** Credenciales incorrectas

**Solución:**
1. Verificar username: debe ser ADMIN (mayúsculas)
2. Verificar password en Oracle Cloud Console
3. Resetear password si es necesario

### Error: "ORA-12541: TNS:no listener"

**Causa:** Servicio incorrecto o Wallet corrupto

**Solución:**
1. Verificar nombre del servicio en tnsnames.ora
2. Re-descargar Wallet desde Oracle Cloud Console
3. Verificar conectividad de red

### Error: "javax.net.ssl.SSLHandshakeException"

**Causa:** Problema con certificados SSL

**Solución:**
1. Verificar que cwallet.sso esté en el Wallet
2. Verificar que sqlnet.ora tenga SSL_SERVER_DN_MATCH=yes
3. Re-descargar Wallet

---

## 📊 Monitoreo y Performance

### Queries de Diagnóstico

```sql
-- Ver sesiones activas
SELECT username, sid, serial#, status 
FROM v$session 
WHERE username = 'ADMIN';

-- Ver tamaño de tablas
SELECT table_name, num_rows, blocks 
FROM user_tables 
ORDER BY num_rows DESC;

-- Ver índices
SELECT index_name, table_name, uniqueness 
FROM user_indexes;

-- Ver estadísticas de queries
SELECT sql_text, executions, elapsed_time 
FROM v$sql 
WHERE sql_text LIKE '%PACIENTES%' 
ORDER BY elapsed_time DESC;
```

### Optimización

```sql
-- Analizar tablas para estadísticas
EXEC DBMS_STATS.GATHER_SCHEMA_STATS('ADMIN');

-- Ver plan de ejecución
EXPLAIN PLAN FOR
SELECT * FROM pacientes WHERE estado = 'CRÍTICO';

SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY);
```

---

## 🎥 Para el Video

**Demostración de Oracle Cloud (2 minutos):**

1. **Mostrar Wallet** (15 seg)
   - Mostrar carpeta Wallet_S58ONUXCX4C1QXE9
   - Explicar que contiene certificados SSL

2. **Conectarse con SQL Developer** (30 seg)
   - Abrir SQL Developer
   - Mostrar conexión configurada
   - Test connection → Success

3. **Mostrar Tablas y Datos** (45 seg)
   - SELECT * FROM pacientes;
   - SELECT * FROM signos_vitales;
   - SELECT * FROM alertas;
   - Mostrar que hay datos de prueba

4. **Ejecutar Query** (30 seg)
   - Query complejo con JOIN
   - Mostrar resultados
   ```sql
   SELECT p.nombre, p.apellido, 
          sv.frecuencia_cardiaca, 
          a.mensaje
   FROM pacientes p
   JOIN signos_vitales sv ON p.id = sv.paciente_id
   LEFT JOIN alertas a ON p.id = a.paciente_id
   WHERE p.estado = 'CRÍTICO';
   ```

---

## 📚 Recursos Adicionales

- [Oracle Autonomous Database Documentation](https://docs.oracle.com/en/cloud/paas/autonomous-database/)
- [Oracle JDBC Driver](https://www.oracle.com/database/technologies/appdev/jdbc.html)
- [Spring Data JPA with Oracle](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)

---

**Última actualización:** 2026-01-22
**Versión:** 1.0
