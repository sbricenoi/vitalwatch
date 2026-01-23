import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../core/services/api.service';
import { ApiResponse } from '../../models/api-response.model';
import { Alerta } from '../../models/alerta.model';

/**
 * Servicio para gestión de alertas
 */
@Injectable({
  providedIn: 'root'
})
export class AlertaService {

  private readonly endpoint = 'alertas';

  constructor(private apiService: ApiService) { }

  /**
   * Obtener todas las alertas
   */
  obtenerTodas(): Observable<ApiResponse<Alerta[]>> {
    return this.apiService.get<ApiResponse<Alerta[]>>(this.endpoint);
  }

  /**
   * Obtener alerta por ID
   */
  obtenerPorId(id: number): Observable<ApiResponse<Alerta>> {
    return this.apiService.get<ApiResponse<Alerta>>(`${this.endpoint}/${id}`);
  }

  /**
   * Obtener alertas activas
   */
  obtenerActivas(): Observable<ApiResponse<Alerta[]>> {
    return this.apiService.get<ApiResponse<Alerta[]>>(`${this.endpoint}/activas`);
  }

  /**
   * Obtener alertas críticas activas
   */
  obtenerCriticas(): Observable<ApiResponse<Alerta[]>> {
    return this.apiService.get<ApiResponse<Alerta[]>>(`${this.endpoint}/criticas`);
  }

  /**
   * Obtener alertas de un paciente
   */
  obtenerPorPaciente(pacienteId: number): Observable<ApiResponse<Alerta[]>> {
    return this.apiService.get<ApiResponse<Alerta[]>>(`${this.endpoint}/paciente/${pacienteId}`);
  }

  /**
   * Obtener alertas activas de un paciente
   */
  obtenerActivasPorPaciente(pacienteId: number): Observable<ApiResponse<Alerta[]>> {
    return this.apiService.get<ApiResponse<Alerta[]>>(`${this.endpoint}/paciente/${pacienteId}/activas`);
  }

  /**
   * Obtener alertas por severidad
   */
  obtenerPorSeveridad(severidad: string): Observable<ApiResponse<Alerta[]>> {
    return this.apiService.get<ApiResponse<Alerta[]>>(`${this.endpoint}/severidad/${severidad}`);
  }

  /**
   * Obtener alertas recientes
   */
  obtenerRecientes(limite: number = 10): Observable<ApiResponse<Alerta[]>> {
    return this.apiService.get<ApiResponse<Alerta[]>>(`${this.endpoint}/recientes?limite=${limite}`);
  }

  /**
   * Crear alerta manual
   */
  crear(alerta: Alerta): Observable<ApiResponse<Alerta>> {
    return this.apiService.post<ApiResponse<Alerta>>(this.endpoint, alerta);
  }

  /**
   * Resolver alerta
   */
  resolver(id: number, resueltoPor: string, notasResolucion: string): Observable<ApiResponse<Alerta>> {
    return this.apiService.put<ApiResponse<Alerta>>(`${this.endpoint}/${id}/resolver`, {
      resueltoPor,
      notasResolucion
    });
  }

  /**
   * Descartar alerta
   */
  descartar(id: number, descartadoPor: string, motivo: string): Observable<ApiResponse<Alerta>> {
    return this.apiService.put<ApiResponse<Alerta>>(`${this.endpoint}/${id}/descartar`, {
      resueltoPor: descartadoPor,
      notasResolucion: motivo
    });
  }

  /**
   * Eliminar alerta
   */
  eliminar(id: number): Observable<ApiResponse<void>> {
    return this.apiService.delete<ApiResponse<void>>(`${this.endpoint}/${id}`);
  }

  /**
   * Obtener estadísticas de alertas
   */
  obtenerEstadisticas(): Observable<ApiResponse<any>> {
    return this.apiService.get<ApiResponse<any>>(`${this.endpoint}/estadisticas`);
  }
}
