import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../core/services/api.service';
import { ApiResponse } from '../../models/api-response.model';
import { Alerta } from '../../models/alerta.model';
import { Paciente } from '../../models/paciente.model';

/**
 * Servicio para obtener datos del dashboard
 */
@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  private readonly endpoint = 'dashboard';

  constructor(private apiService: ApiService) { }

  /**
   * Obtener estadísticas generales
   */
  obtenerEstadisticas(): Observable<ApiResponse<any>> {
    return this.apiService.get<ApiResponse<any>>(`${this.endpoint}/estadisticas`);
  }

  /**
   * Obtener pacientes por estado
   */
  obtenerPacientesPorEstado(): Observable<ApiResponse<any>> {
    return this.apiService.get<ApiResponse<any>>(`${this.endpoint}/pacientes-por-estado`);
  }

  /**
   * Obtener alertas recientes
   */
  obtenerAlertasRecientes(limite: number = 10): Observable<ApiResponse<Alerta[]>> {
    return this.apiService.get<ApiResponse<Alerta[]>>(`${this.endpoint}/alertas-recientes?limite=${limite}`);
  }

  /**
   * Obtener pacientes críticos
   */
  obtenerPacientesCriticos(): Observable<ApiResponse<Paciente[]>> {
    return this.apiService.get<ApiResponse<Paciente[]>>(`${this.endpoint}/pacientes-criticos`);
  }

  /**
   * Obtener alertas por severidad
   */
  obtenerAlertasPorSeveridad(): Observable<ApiResponse<any>> {
    return this.apiService.get<ApiResponse<any>>(`${this.endpoint}/alertas-por-severidad`);
  }
}
