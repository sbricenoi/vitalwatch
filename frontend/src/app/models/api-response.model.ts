/**
 * Modelo de respuesta estándar de la API
 */
export interface ApiResponse<T> {
  traceId: string;
  code: string;
  message: string;
  data: T;
}
