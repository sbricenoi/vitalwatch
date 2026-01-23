import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../core/services/api.service';
import { ApiResponse } from '../../models/api-response.model';
import { SignosVitales } from '../../models/signos-vitales.model';

/**
 * Servicio para gestión de signos vitales
 */
@Injectable({
  providedIn: 'root'
})
export class SignosVitalesService {

  private readonly endpoint = 'signos-vitales';

  constructor(private apiService: ApiService) { }

  /**
   * Obtener todos los registros de signos vitales
   */
  obtenerTodos(): Observable<ApiResponse<SignosVitales[]>> {
    return this.apiService.get<ApiResponse<SignosVitales[]>>(this.endpoint);
  }

  /**
   * Obtener signos vitales por ID
   */
  obtenerPorId(id: number): Observable<ApiResponse<SignosVitales>> {
    return this.apiService.get<ApiResponse<SignosVitales>>(`${this.endpoint}/${id}`);
  }

  /**
   * Obtener historial de signos vitales de un paciente
   */
  obtenerPorPaciente(pacienteId: number): Observable<ApiResponse<SignosVitales[]>> {
    return this.apiService.get<ApiResponse<SignosVitales[]>>(`${this.endpoint}/paciente/${pacienteId}`);
  }

  /**
   * Obtener último registro de un paciente
   */
  obtenerUltimoRegistro(pacienteId: number): Observable<ApiResponse<SignosVitales>> {
    return this.apiService.get<ApiResponse<SignosVitales>>(`${this.endpoint}/paciente/${pacienteId}/ultimo`);
  }

  /**
   * Obtener últimos N registros de un paciente
   */
  obtenerUltimosRegistros(pacienteId: number, limite: number = 10): Observable<ApiResponse<SignosVitales[]>> {
    return this.apiService.get<ApiResponse<SignosVitales[]>>(`${this.endpoint}/paciente/${pacienteId}/ultimos?limite=${limite}`);
  }

  /**
   * Registrar nuevos signos vitales
   */
  registrar(signosVitales: SignosVitales): Observable<ApiResponse<SignosVitales>> {
    return this.apiService.post<ApiResponse<SignosVitales>>(this.endpoint, signosVitales);
  }

  /**
   * Actualizar signos vitales
   */
  actualizar(id: number, signosVitales: SignosVitales): Observable<ApiResponse<SignosVitales>> {
    return this.apiService.put<ApiResponse<SignosVitales>>(`${this.endpoint}/${id}`, signosVitales);
  }

  /**
   * Eliminar registro de signos vitales
   */
  eliminar(id: number): Observable<ApiResponse<void>> {
    return this.apiService.delete<ApiResponse<void>>(`${this.endpoint}/${id}`);
  }
}
