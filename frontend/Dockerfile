# Etapa 1: Construcción
FROM node:20-alpine AS build

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de npm
COPY package*.json ./

# Instalar dependencias (incluye dev para el build)
# Usando --legacy-peer-deps para resolver conflictos de versiones
RUN npm install --legacy-peer-deps

# Copiar el código fuente
COPY . .

# Construir la aplicación para producción
RUN npm run build --prod

# Etapa 2: Servidor Nginx
FROM nginx:alpine

# Copiar archivos de build al directorio de Nginx
COPY --from=build /app/dist/vitalwatch /usr/share/nginx/html

# Copiar configuración personalizada de Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Exponer puerto
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:80 || exit 1

# Comando por defecto
CMD ["nginx", "-g", "daemon off;"]
