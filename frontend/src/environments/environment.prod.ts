/**
 * Configuración de entorno para producción
 */
export const environment = {
  production: true,
  apiUrl: 'https://api.vitalwatch.com/api/v1',
  auth: {
    domain: 'your-tenant.auth0.com',
    clientId: 'your-client-id',
    redirectUri: window.location.origin,
    audience: 'https://api.vitalwatch.com'
  }
};
