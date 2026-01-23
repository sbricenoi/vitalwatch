import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../core/services/api.service';
import { ApiResponse } from '../../models/api-response.model';
import { Paciente } from '../../models/paciente.model';

/**
 * Servicio para gestión de pacientes
 */
@Injectable({
  providedIn: 'root'
})
export class PacienteService {

  private readonly endpoint = 'pacientes';

  constructor(private apiService: ApiService) { }

  /**
   * Obtener todos los pacientes
   */
  obtenerTodos(): Observable<ApiResponse<Paciente[]>> {
    return this.apiService.get<ApiResponse<Paciente[]>>(this.endpoint);
  }

  /**
   * Obtener paciente por ID
   */
  obtenerPorId(id: number): Observable<ApiResponse<Paciente>> {
    return this.apiService.get<ApiResponse<Paciente>>(`${this.endpoint}/${id}`);
  }

  /**
   * Crear nuevo paciente
   */
  crear(paciente: Paciente): Observable<ApiResponse<Paciente>> {
    return this.apiService.post<ApiResponse<Paciente>>(this.endpoint, paciente);
  }

  /**
   * Actualizar paciente existente
   */
  actualizar(id: number, paciente: Paciente): Observable<ApiResponse<Paciente>> {
    return this.apiService.put<ApiResponse<Paciente>>(`${this.endpoint}/${id}`, paciente);
  }

  /**
   * Eliminar paciente
   */
  eliminar(id: number): Observable<ApiResponse<void>> {
    return this.apiService.delete<ApiResponse<void>>(`${this.endpoint}/${id}`);
  }

  /**
   * Buscar pacientes por estado
   */
  buscarPorEstado(estado: string): Observable<ApiResponse<Paciente[]>> {
    return this.apiService.get<ApiResponse<Paciente[]>>(`${this.endpoint}/estado/${estado}`);
  }
  
  /**
   * Obtener pacientes por sala
   */
  obtenerPorSala(sala: string): Observable<ApiResponse<Paciente[]>> {
    return this.apiService.get<ApiResponse<Paciente[]>>(`${this.endpoint}/sala/${sala}`);
  }

  /**
   * Obtener pacientes críticos
   */
  obtenerCriticos(): Observable<ApiResponse<Paciente[]>> {
    return this.apiService.get<ApiResponse<Paciente[]>>(`${this.endpoint}/criticos`);
  }

  /**
   * Buscar pacientes
   */
  buscar(termino: string): Observable<ApiResponse<Paciente[]>> {
    return this.apiService.get<ApiResponse<Paciente[]>>(`${this.endpoint}/buscar?q=${termino}`);
  }
}
