/**
 * Modelo de Signos Vitales
 * Coincide con SignosVitalesDTO del backend
 */
export interface SignosVitales {
  id?: number;
  pacienteId: number;
  pacienteNombre?: string;
  pacienteSala?: string;
  pacienteCama?: string;
  frecuenciaCardiaca: number;
  presionSistolica: number;  // Corregido para coincidir con backend
  presionDiastolica: number;  // Corregido para coincidir con backend
  temperatura: number;
  saturacionOxigeno: number;
  frecuenciaRespiratoria: number;
  estadoConciencia: string;  // Nuevo campo obligatorio
  registradoPor: string;     // Nuevo campo obligatorio
  observaciones?: string;
  fechaRegistro?: string;
  tieneAlgunaAnormalidad?: boolean;
  esCritico?: boolean;
}
