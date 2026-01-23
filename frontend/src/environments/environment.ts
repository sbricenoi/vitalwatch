/**
 * Configuración de entorno para desarrollo
 */
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1',
  auth: {
    domain: 'your-tenant.auth0.com',
    clientId: 'your-client-id',
    redirectUri: window.location.origin,
    audience: 'https://api.vitalwatch.com'
  }
};
