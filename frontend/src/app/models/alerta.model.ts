/**
 * Modelo de Alerta
 * Coincide con AlertaDTO del backend
 */
export interface Alerta {
  id?: number;
  pacienteId: number;
  
  // Información del paciente (campos calculados en backend)
  pacienteNombre?: string;
  pacienteSala?: string;
  pacienteCama?: string;
  pacienteEstado?: string;
  
  // Datos de la alerta
  tipo: string;                          // Backend: "tipo" (no "tipoAlerta")
  mensaje: string;
  severidad: 'BAJA' | 'MODERADA' | 'CRÍTICA';
  estado: 'ACTIVA' | 'RESUELTA' | 'DESCARTADA';
  
  // Fechas
  fechaCreacion?: string;                // Backend: "fechaCreacion" (no "fechaGeneracion")
  fechaResolucion?: string;
  
  // Resolución
  resueltoPor?: string;
  notasResolucion?: string;              // Backend: "notasResolucion" (no "observacionesResolucion")
  
  // Campos calculados (solo en responses)
  estaActiva?: boolean;
  esCritica?: boolean;
  tiempoTranscurridoMinutos?: number;
}
