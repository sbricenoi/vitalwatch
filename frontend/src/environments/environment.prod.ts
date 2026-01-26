/**
 * Configuración de entorno para producción
 */
export const environment = {
  production: true,
  apiUrl: 'https://vitalwatch-backend.graycoast-fc35a2d0.southcentralus.azurecontainerapps.io/api/v1',
  auth: {
    domain: 'your-tenant.auth0.com',
    clientId: 'your-client-id',
    redirectUri: window.location.origin,
    audience: 'https://vitalwatch-backend.graycoast-fc35a2d0.southcentralus.azurecontainerapps.io'
  }
};
