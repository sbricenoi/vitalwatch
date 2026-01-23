import { Component, OnInit } from '@angular/core';
import { PacienteService } from '../../shared/services/paciente.service';
import { Paciente } from '../../models/paciente.model';

@Component({
  selector: 'app-pacientes-list',
  template: `
    <div class="container">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2 class="mb-0">Listado de Pacientes</h2>
        <button class="btn btn-primary" routerLink="/pacientes/nuevo">
          <i class="bi bi-plus-circle"></i> Nuevo Paciente
        </button>
      </div>
      
      <div *ngIf="cargando" class="text-center">
        <div class="spinner-border" role="status">
          <span class="visually-hidden">Cargando...</span>
        </div>
      </div>

      <div *ngIf="!cargando" class="row">
        <div *ngFor="let paciente of pacientes" class="col-md-6 mb-3">
          <div class="card">
            <div class="card-body">
              <div class="d-flex justify-content-between align-items-start">
                <div>
                  <h5 class="card-title">{{ paciente.nombre }} {{ paciente.apellido }}</h5>
                  <p class="card-text">
                    <span [ngClass]="'badge bg-' + getEstadoBadge(paciente.estado)">{{ paciente.estado }}</span><br>
                    <strong>Sala:</strong> {{ paciente.sala || 'N/A' }} - <strong>Cama:</strong> {{ paciente.cama || 'N/A' }}<br>
                    <small class="text-muted">{{ paciente.diagnostico || 'Sin diagnóstico' }}</small>
                  </p>
                </div>
                <div class="btn-group" role="group">
                  <button class="btn btn-sm btn-outline-primary" 
                          [routerLink]="['/pacientes/editar', paciente.id]">
                    Editar
                  </button>
                  <button class="btn btn-sm btn-outline-info"
                          [routerLink]="['/signos-vitales']"
                          [queryParams]="{pacienteId: paciente.id}">
                    Signos
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div *ngIf="!cargando && pacientes.length === 0" class="alert alert-info">
        No hay pacientes registrados. <a routerLink="/pacientes/nuevo">Crear el primero</a>
      </div>
    </div>
  `
})
export class PacientesListComponent implements OnInit {
  
  pacientes: Paciente[] = [];
  cargando = false;

  constructor(private pacienteService: PacienteService) { }

  ngOnInit(): void {
    this.cargarPacientes();
  }

  cargarPacientes(): void {
    this.cargando = true;
    this.pacienteService.obtenerTodos().subscribe({
      next: (response) => {
        this.pacientes = response.data;
        this.cargando = false;
      },
      error: (error) => {
        console.error('Error al cargar pacientes:', error);
        this.cargando = false;
      }
    });
  }

  getEstadoBadge(estado: string): string {
    const badges: Record<string, string> = {
      'CRÍTICO': 'danger',
      'MODERADO': 'warning',
      'ESTABLE': 'success',
      'RECUPERACIÓN': 'info'
    };
    return badges[estado] || 'secondary';
  }
}
