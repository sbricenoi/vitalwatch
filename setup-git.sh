#!/bin/bash

# ============================================================================
# VitalWatch - Configuración de Git y GitHub
# ============================================================================
# Script para inicializar el repositorio y prepararlo para GitHub
# ============================================================================

# Colores
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

clear

echo -e "${BLUE}============================================================================${NC}"
echo -e "${BLUE}  VitalWatch - Configuración de Git y GitHub${NC}"
echo -e "${BLUE}============================================================================${NC}"
echo ""

# ============================================================================
# 1. Configurar usuario Git
# ============================================================================

echo -e "${BLUE}📝 Configuración de Usuario Git${NC}"
echo ""
read -p "Ingresa tu nombre completo: " git_name
read -p "Ingresa tu email de GitHub: " git_email
echo ""

# Configurar Git
git config user.name "$git_name"
git config user.email "$git_email"

echo -e "${GREEN}✅ Usuario configurado:${NC}"
echo -e "   Nombre: $git_name"
echo -e "   Email:  $git_email"
echo ""

# ============================================================================
# 2. Inicializar repositorio
# ============================================================================

echo -e "${BLUE}🔧 Inicializando repositorio Git...${NC}"

if [ ! -d .git ]; then
    git init
    echo -e "${GREEN}✅ Repositorio Git inicializado${NC}"
else
    echo -e "${YELLOW}⚠️  El repositorio Git ya existe${NC}"
fi
echo ""

# ============================================================================
# 3. Verificar .gitignore
# ============================================================================

echo -e "${BLUE}📋 Verificando archivos sensibles...${NC}"

if [ -f .gitignore ]; then
    echo -e "${GREEN}✅ .gitignore encontrado${NC}"
    
    # Verificar que el Wallet esté en .gitignore
    if grep -q "Wallet_S58ONUXCX4C1QXE9" .gitignore; then
        echo -e "${GREEN}✅ Wallet protegido en .gitignore${NC}"
    else
        echo -e "${RED}❌ ADVERTENCIA: Wallet NO está en .gitignore${NC}"
    fi
    
    # Verificar application.properties
    if grep -q "application-.*\.properties" .gitignore; then
        echo -e "${GREEN}✅ Archivos de configuración protegidos${NC}"
    else
        echo -e "${YELLOW}⚠️  Verifica que application.properties no tenga credenciales${NC}"
    fi
else
    echo -e "${RED}❌ ERROR: .gitignore no encontrado${NC}"
    exit 1
fi
echo ""

# ============================================================================
# 4. Agregar archivos
# ============================================================================

echo -e "${BLUE}📦 Agregando archivos al repositorio...${NC}"

git add .

# Mostrar archivos que se van a commitear
echo ""
echo -e "${BLUE}Archivos a commitear:${NC}"
git status --short | head -20
echo ""

# ============================================================================
# 5. Crear commit inicial
# ============================================================================

echo -e "${BLUE}💾 Creando commit inicial...${NC}"
echo ""

read -p "¿Deseas crear el commit inicial? (s/n): " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Ss]$ ]]; then
    git commit -m "🎉 Initial commit: VitalWatch - Sistema de Monitoreo de Signos Vitales

- Backend: Spring Boot 3.2 + Java 17
- Frontend: Angular 17 + Bootstrap 5
- API Gateway: Kong
- Database: Oracle Cloud Autonomous Database
- Dockerizado con docker-compose
- Script de despliegue automático (deploy.sh)
- Documentación completa con diagramas
- 42 endpoints REST documentados con Swagger
- Sistema de autenticación con roles
- Generación automática de alertas médicas"
    
    echo ""
    echo -e "${GREEN}✅ Commit inicial creado${NC}"
else
    echo -e "${YELLOW}⚠️  Commit omitido. Puedes crearlo manualmente con:${NC}"
    echo -e "   git commit -m \"Initial commit\""
fi
echo ""

# ============================================================================
# 6. Instrucciones para GitHub
# ============================================================================

echo -e "${BLUE}============================================================================${NC}"
echo -e "${BLUE}  Siguiente Paso: Crear Repositorio en GitHub${NC}"
echo -e "${BLUE}============================================================================${NC}"
echo ""
echo "📝 Instrucciones:"
echo ""
echo "1. Ve a GitHub: https://github.com/new"
echo ""
echo "2. Crea un nuevo repositorio:"
echo "   Nombre:       vitalwatch"
echo "   Descripción:  Sistema Cloud Native de Monitoreo de Signos Vitales"
echo "   Visibilidad:  Público o Privado (tu elección)"
echo "   ⚠️  NO marques: Initialize with README, .gitignore, or license"
echo ""
echo "3. Después de crear el repo, ejecuta estos comandos:"
echo ""
echo -e "${GREEN}   # Cambiar rama a 'main'${NC}"
echo "   git branch -M main"
echo ""
echo -e "${GREEN}   # Agregar remote de GitHub (reemplaza TU_USUARIO)${NC}"
echo "   git remote add origin https://github.com/TU_USUARIO/vitalwatch.git"
echo ""
echo -e "${GREEN}   # Subir código${NC}"
echo "   git push -u origin main"
echo ""
echo "============================================================================"
echo ""
echo -e "${YELLOW}⚠️  IMPORTANTE: Verifica que NO se suban credenciales${NC}"
echo ""
echo "Archivos que NO deben subirse:"
echo "  ❌ Wallet_S58ONUXCX4C1QXE9/ (credenciales de Oracle)"
echo "  ❌ application-*.properties (contraseñas)"
echo "  ❌ .env (variables de entorno)"
echo ""
echo "============================================================================"
echo ""
