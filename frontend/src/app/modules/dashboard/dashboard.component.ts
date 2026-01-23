import { Component, OnInit } from '@angular/core';
import { DashboardService } from '../../shared/services/dashboard.service';
import { AlertaService } from '../../shared/services/alerta.service';
import { Alerta } from '../../models/alerta.model';
import { Paciente } from '../../models/paciente.model';

/**
 * Componente Dashboard Principal
 * Muestra estadísticas generales y alertas críticas
 */
@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {

  estadisticas: any = {};
  alertasRecientes: Alerta[] = [];
  pacientesCriticos: Paciente[] = [];
  cargando = false;
  error: string | null = null;

  constructor(
    private dashboardService: DashboardService,
    private alertaService: AlertaService
  ) { }

  ngOnInit(): void {
    this.cargarDatos();
  }

  /**
   * Cargar todos los datos del dashboard
   */
  cargarDatos(): void {
    this.cargando = true;
    this.error = null;

    // Cargar estadísticas
    this.dashboardService.obtenerEstadisticas().subscribe({
      next: (response) => {
        this.estadisticas = response.data;
      },
      error: (error) => {
        console.error('Error al cargar estadísticas:', error);
        this.error = 'Error al cargar estadísticas';
      }
    });

    // Cargar alertas recientes
    this.dashboardService.obtenerAlertasRecientes(5).subscribe({
      next: (response) => {
        this.alertasRecientes = response.data;
      },
      error: (error) => {
        console.error('Error al cargar alertas:', error);
      }
    });

    // Cargar pacientes críticos
    this.dashboardService.obtenerPacientesCriticos().subscribe({
      next: (response) => {
        this.pacientesCriticos = response.data;
        this.cargando = false;
      },
      error: (error) => {
        console.error('Error al cargar pacientes críticos:', error);
        this.cargando = false;
      }
    });
  }

  /**
   * Obtener clase CSS según severidad de alerta
   */
  getSeveridadClass(severidad: string): string {
    const classes: Record<string, string> = {
      'CRÍTICA': 'text-danger',
      'MODERADA': 'text-warning',
      'BAJA': 'text-info'
    };
    return classes[severidad] || 'text-secondary';
  }

  /**
   * Obtener clase CSS según estado del paciente
   */
  getEstadoClass(estado: string): string {
    const classes: Record<string, string> = {
      'CRÍTICO': 'badge-estado critico',
      'MODERADO': 'badge-estado moderado',
      'ESTABLE': 'badge-estado estable',
      'RECUPERACIÓN': 'badge-estado estable'
    };
    return classes[estado] || 'badge-estado';
  }
}
