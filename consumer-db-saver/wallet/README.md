# 🔐 Oracle Cloud Wallet

## ⚠️ IMPORTANTE

Esta carpeta debe contener el **Oracle Cloud Wallet** para conectarse a la base de datos.

**NO SUBIR ESTOS ARCHIVOS A GITHUB** - Contienen credenciales sensibles.

---

## 📁 Archivos Requeridos

Esta carpeta debe contener los siguientes archivos:

```
Wallet_S58ONUXCX4C1QXE9/
├── cwallet.sso          # Wallet SSO (Oracle Single Sign-On)
├── keystore.jks         # Java KeyStore
├── truststore.jks       # Trust Store
├── tnsnames.ora         # Configuración de TNS
├── sqlnet.ora           # Configuración de SQL*Net
├── ojdbc.properties     # Propiedades de JDBC
└── README               # Documentación del Wallet
```

---

## 📥 Cómo Obtener el Wallet

### 1. Descargar desde Oracle Cloud

1. Accede a [Oracle Cloud Console](https://cloud.oracle.com/)
2. Navega a **Autonomous Database**
3. Selecciona tu base de datos: `s58onuxcx4c1qxe9`
4. Click en **DB Connection**
5. Click en **Download Wallet**
6. Ingresa una contraseña para el Wallet
7. Descomprime el archivo ZIP en esta carpeta

### 2. Configurar el Wallet

Después de descargar, debes configurar los archivos:

#### A. Editar `sqlnet.ora`

```properties
WALLET_LOCATION = (SOURCE = (METHOD = file) (METHOD_DATA = (DIRECTORY="?/network/admin")))
SSL_SERVER_DN_MATCH=yes
SQLNET.WALLET_OVERRIDE = TRUE
```

#### B. Editar `ojdbc.properties`

Descomentar y configurar las propiedades de JKS:

```properties
# JKS Configuration
javax.net.ssl.trustStore=/app/wallet/truststore.jks
javax.net.ssl.trustStorePassword=TU_PASSWORD_AQUI
javax.net.ssl.keyStore=/app/wallet/keystore.jks
javax.net.ssl.keyStorePassword=TU_PASSWORD_AQUI

# SSO Configuration (comentar si usas JKS)
# oracle.net.wallet_location=(SOURCE=(METHOD=FILE)(METHOD_DATA=(DIRECTORY=${TNS_ADMIN})))
```

---

## 🔧 Configuración del Proyecto

### Variables de Entorno

En `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCPS)(HOST=adb.sa-santiago-1.oraclecloud.com)(PORT=1522))(CONNECT_DATA=(SERVICE_NAME=s58onuxcx4c1qxe9_high.adb.oraclecloud.com))(SECURITY=(SSL_SERVER_DN_MATCH=YES)))
spring.datasource.username=ADMIN
spring.datasource.password=TU_PASSWORD_AQUI
```

### Docker Compose

En `docker-compose.yml`, el Wallet se monta como volumen:

```yaml
volumes:
  - ./Wallet_S58ONUXCX4C1QXE9:/app/wallet:ro
```

---

## ✅ Verificación

Para verificar que el Wallet está configurado correctamente:

```bash
# Listar archivos
ls -la Wallet_S58ONUXCX4C1QXE9/

# Verificar permisos
chmod 644 Wallet_S58ONUXCX4C1QXE9/*

# Probar conexión
docker-compose up backend
# Revisar logs: docker-compose logs backend
```

---

## 🆘 Troubleshooting

### Error: "Unable to initialize the key store"

**Solución**: Verifica que `ojdbc.properties` tenga configurados los JKS correctamente.

### Error: "Wallet not found"

**Solución**: Asegúrate de que los archivos estén en la carpeta correcta y con los permisos adecuados.

### Error: "Invalid credentials"

**Solución**: Verifica la contraseña en `application.properties`.

---

## 📚 Documentación

- [Oracle Autonomous Database](https://docs.oracle.com/en-us/iaas/Content/Database/Concepts/adboverview.htm)
- [Oracle Wallet](https://docs.oracle.com/en/cloud/paas/autonomous-database/adbsa/connect-download-wallet.html)
- [Guía del proyecto](../docs/guia-oracle-cloud.md)

---

**Última actualización**: 2026-01-23
