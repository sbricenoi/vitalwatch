# 🎨 Frontend - VitalWatch

## Aplicación Angular

### 📋 Descripción
Frontend del sistema VitalWatch desarrollado con Angular 17+ y Angular Material. Proporciona una interfaz intuitiva para médicos y personal de salud para monitorear pacientes críticos y gestionar alertas médicas.

---

## 🚀 Tecnologías

- **Angular 17+** - Framework principal
- **TypeScript 5.x** - Lenguaje de programación
- **Angular Material** - Componentes UI
- **RxJS 7.x** - Programación reactiva
- **Chart.js** - Gráficos de signos vitales
- **Socket.io-client** - WebSockets para tiempo real
- **Auth0/Keycloak SDK** - Autenticación

---

## 📁 Estructura del Proyecto

```
frontend/
├── src/
│   ├── app/
│   │   ├── core/              # Servicios core
│   │   │   ├── auth/          # Autenticación
│   │   │   └── services/      # Servicios base
│   │   │
│   │   ├── modules/           # Módulos funcionales
│   │   │   ├── login/
│   │   │   ├── dashboard/
│   │   │   ├── pacientes/
│   │   │   ├── signos-vitales/
│   │   │   └── alertas/
│   │   │
│   │   ├── shared/            # Compartidos
│   │   │   └── components/
│   │   │
│   │   └── models/            # Interfaces
│   │
│   ├── environments/          # Configuraciones
│   ├── assets/               # Recursos estáticos
│   └── styles.scss           # Estilos globales
│
├── package.json
├── angular.json
├── Dockerfile
└── README.md
```

---

## ⚙️ Configuración

### Prerequisites
- Node.js 18+ y npm
- Angular CLI 17+
- Editor (VS Code recomendado)

### Instalar Angular CLI

```bash
npm install -g @angular/cli
```

### Instalar Dependencias

```bash
npm install
```

### Configurar Variables de Entorno

Editar `src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1',
  auth0Domain: 'your-domain.auth0.com',
  auth0ClientId: 'your-client-id',
  auth0Audience: 'your-api-identifier'
};
```

---

## 🏃 Ejecutar Localmente

### Desarrollo

```bash
# Modo desarrollo
ng serve

# Con puerto específico
ng serve --port 4200

# Abrir automáticamente en navegador
ng serve --open
```

La aplicación estará disponible en: `http://localhost:4200`

### Build Producción

```bash
# Build
ng build --configuration production

# Build con análisis de bundle
ng build --configuration production --stats-json
npm run webpack-bundle-analyzer
```

---

## 🐳 Docker

### Build

```bash
docker build -t vitalwatch-frontend:latest .
```

### Run

```bash
docker run -p 80:80 vitalwatch-frontend:latest
```

---

## 🎨 Módulos Principales

### 1. Login
- Autenticación con IdaaS (Auth0/Keycloak)
- Validación de credenciales
- Gestión de tokens JWT
- Redirección automática

### 2. Dashboard
- Vista general del sistema
- Estadísticas en tiempo real
- Lista de pacientes críticos
- Alertas activas
- Gráficos y métricas

### 3. Pacientes
- **Lista**: Visualización de todos los pacientes
- **Detalle**: Información completa del paciente
- **Crear/Editar**: Formulario con validaciones
- **Eliminar**: Con confirmación

### 4. Signos Vitales
- **Monitor**: Vista en tiempo real de signos vitales
- **Historial**: Gráficos de tendencias
- **Registro**: Formulario para ingresar mediciones
- **Rangos**: Indicadores visuales de valores normales/anormales

### 5. Alertas
- **Lista de alertas activas**: Con filtros y ordenamiento
- **Detalle de alerta**: Información completa
- **Resolución**: Marcar alerta como resuelta
- **Notificaciones**: En tiempo real con WebSockets

---

## 🔐 Autenticación

### AuthGuard

Protege rutas que requieren autenticación:

```typescript
const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { 
    path: 'dashboard', 
    component: DashboardComponent,
    canActivate: [AuthGuard] 
  }
];
```

### AuthInterceptor

Añade automáticamente el token JWT a todas las peticiones:

```typescript
// Configurado automáticamente en app.module.ts
providers: [
  { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
]
```

---

## 🧪 Testing

### Tests Unitarios

```bash
# Ejecutar tests
ng test

# Con cobertura
ng test --code-coverage

# Watch mode
ng test --watch
```

### Tests E2E

```bash
# Ejecutar E2E
ng e2e
```

### Reporte de Cobertura

```
coverage/index.html
```

---

## 🎨 Theming y Estilos

### Angular Material Theme

Personalizado en `styles.scss`:

```scss
@use '@angular/material' as mat;

$vitalwatch-primary: mat.define-palette(mat.$blue-palette);
$vitalwatch-accent: mat.define-palette(mat.$pink-palette);
$vitalwatch-warn: mat.define-palette(mat.$red-palette);

$vitalwatch-theme: mat.define-light-theme((
  color: (
    primary: $vitalwatch-primary,
    accent: $vitalwatch-accent,
    warn: $vitalwatch-warn,
  )
));

@include mat.all-component-themes($vitalwatch-theme);
```

### Responsive Design

Breakpoints configurados:

- **Mobile**: < 600px
- **Tablet**: 600px - 960px
- **Desktop**: > 960px

---

## 📊 Chart.js

### Configuración de Gráficos

```typescript
// Ejemplo: Gráfico de frecuencia cardíaca
chartOptions = {
  responsive: true,
  scales: {
    y: {
      beginAtZero: false,
      min: 40,
      max: 180
    }
  },
  plugins: {
    annotation: {
      annotations: {
        line1: {
          type: 'line',
          yMin: 60,
          yMax: 60,
          borderColor: 'green',
          borderWidth: 2,
          label: {
            content: 'Mínimo normal'
          }
        }
      }
    }
  }
};
```

---

## 🔔 WebSockets (Tiempo Real)

### Conexión

```typescript
import { io } from 'socket.io-client';

const socket = io('http://localhost:8080', {
  auth: {
    token: this.authService.getToken()
  }
});

// Escuchar alertas
socket.on('nueva-alerta', (alerta) => {
  this.mostrarNotificacion(alerta);
});
```

---

## 📝 Validaciones de Formularios

### Reactive Forms con Validaciones

```typescript
pacienteForm = this.fb.group({
  nombre: ['', [Validators.required, Validators.minLength(2)]],
  apellido: ['', [Validators.required, Validators.minLength(2)]],
  rut: ['', [Validators.required, this.rutValidator]],
  edad: ['', [Validators.required, Validators.min(0), Validators.max(120)]],
  sala: ['', Validators.required],
  cama: ['', Validators.required]
});
```

---

## 🚧 Troubleshooting

### Error CORS

```
Verificar:
- Backend tiene CORS configurado correctamente
- URL del API correcta en environment.ts
- Headers correctos en las peticiones
```

### Error Auth0

```
Verificar:
- Domain y ClientId correctos
- Callback URL configurada en Auth0
- Scope solicitado correctamente
```

### Error de Compilación

```bash
# Limpiar caché
rm -rf node_modules
rm package-lock.json
npm install

# Limpiar caché de Angular
ng cache clean
```

---

## 📦 Scripts Disponibles

```bash
# Desarrollo
npm start

# Build producción
npm run build

# Tests
npm test

# E2E
npm run e2e

# Linting
npm run lint

# Formateo
npm run format
```

---

## 🎨 Componentes Reutilizables

### Loading Spinner

```typescript
<app-loading *ngIf="isLoading"></app-loading>
```

### Confirmación de Diálogo

```typescript
const dialogRef = this.dialog.open(ConfirmDialogComponent, {
  data: { message: '¿Está seguro?' }
});

dialogRef.afterClosed().subscribe(result => {
  if (result) {
    // Acción confirmada
  }
});
```

---

## 📚 Referencias

- [Angular Documentation](https://angular.io/docs)
- [Angular Material](https://material.angular.io/)
- [RxJS](https://rxjs.dev/)
- [Chart.js](https://www.chartjs.org/)
- [Auth0 Angular SDK](https://github.com/auth0/auth0-angular)

---

## 🎯 Mejores Prácticas Implementadas

✅ Módulos lazy-loaded para mejor performance
✅ OnPush change detection donde es posible
✅ Unsubscribe de observables en ngOnDestroy
✅ Manejo de errores centralizado
✅ Loading states en todas las operaciones
✅ Validaciones exhaustivas en formularios
✅ Responsive design mobile-first
✅ Accesibilidad (ARIA labels)

---

**Desarrollado por:** [Nombre del Equipo]
**Fecha:** Enero 2026
