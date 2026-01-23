/**
 * Modelo de Paciente
 * Coincide con PacienteDTO del backend
 */
export interface Paciente {
  id?: number;
  nombre: string;
  apellido: string;
  rut: string;                    // Campo obligatorio en backend
  fechaNacimiento: string;        // LocalDate en backend
  edad: number;                   // Campo obligatorio en backend
  genero: string;                 // M, F, O
  sala: string;                   // Campo obligatorio en backend
  cama: string;                   // Campo obligatorio en backend
  estado: 'ESTABLE' | 'MODERADO' | 'CRÍTICO' | 'RECUPERACIÓN';
  diagnostico?: string;
  fechaIngreso?: string;          // LocalDateTime en backend (antes fechaAdmision)
  fechaAlta?: string;             // LocalDateTime en backend (antes fechaActualizacion)
  nombreCompleto?: string;        // Campo calculado en backend
  esCritico?: boolean;            // Campo calculado en backend
}
