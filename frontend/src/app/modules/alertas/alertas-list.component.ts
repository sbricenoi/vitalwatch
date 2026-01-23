import { Component, OnInit } from '@angular/core';
import { AlertaService } from '../../shared/services/alerta.service';
import { Alerta } from '../../models/alerta.model';

@Component({
  selector: 'app-alertas-list',
  template: `
    <div class="container">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2 class="mb-0">🔔 Alertas Médicas</h2>
        <div class="btn-group" role="group">
          <button class="btn" [class.btn-primary]="filtroActivo === 'todas'" (click)="filtrar('todas')">
            Todas
          </button>
          <button class="btn" [class.btn-warning]="filtroActivo === 'activas'" (click)="filtrar('activas')">
            Activas
          </button>
          <button class="btn" [class.btn-danger]="filtroActivo === 'criticas'" (click)="filtrar('criticas')">
            Críticas
          </button>
        </div>
      </div>
      
      <div *ngIf="cargando" class="text-center">
        <div class="spinner-border" role="status">
          <span class="visually-hidden">Cargando...</span>
        </div>
      </div>

      <div *ngIf="!cargando && alertas.length === 0" class="alert alert-success">
        ✅ No hay alertas {{ filtroActivo }} en este momento.
      </div>

      <div *ngIf="!cargando" class="row">
        <div *ngFor="let alerta of alertas" class="col-12 mb-3">
          <div class="card" [ngClass]="'border-start border-5 border-' + getSeveridadClass(alerta.severidad)">
            <div class="card-body">
              <div class="d-flex justify-content-between align-items-start">
                <div class="flex-grow-1">
                  <div class="d-flex align-items-center mb-2">
                    <h5 class="card-title mb-0 me-2">{{ alerta.tipo }}</h5>
                    <span class="badge" [ngClass]="'bg-' + getSeveridadClass(alerta.severidad)">
                      {{ alerta.severidad }}
                    </span>
                    <span class="badge bg-secondary ms-2">{{ alerta.estado }}</span>
                  </div>
                  <p class="card-text mb-2">{{ alerta.mensaje }}</p>
                  <small class="text-muted">
                    <strong>Paciente:</strong> {{ alerta.pacienteNombre || 'ID: ' + alerta.pacienteId }}
                    <span *ngIf="alerta.pacienteSala"> | <strong>Sala:</strong> {{ alerta.pacienteSala }} - {{ alerta.pacienteCama }}</span><br>
                    <strong>Generada:</strong> {{ alerta.fechaCreacion | date:'short' }}
                  </small>
                </div>
                <div class="ms-3" *ngIf="alerta.estado === 'ACTIVA'">
                  <div class="btn-group-vertical" role="group">
                    <button class="btn btn-sm btn-success" (click)="mostrarModalResolver(alerta)">
                      ✓ Resolver
                    </button>
                    <button class="btn btn-sm btn-outline-secondary" (click)="mostrarModalDescartar(alerta)">
                      ✗ Descartar
                    </button>
                  </div>
                </div>
              </div>
              
              <!-- Info de Resolución -->
              <div *ngIf="alerta.estado === 'RESUELTA' || alerta.estado === 'DESCARTADA'" 
                   class="mt-3 pt-3 border-top">
                <small class="text-muted">
                  <strong>{{ alerta.estado === 'RESUELTA' ? 'Resuelta' : 'Descartada' }} por:</strong> 
                  {{ alerta.resueltoPor }}<br>
                  <strong>Fecha:</strong> {{ alerta.fechaResolucion | date:'short' }}<br>
                  <strong>Notas:</strong> {{ alerta.notasResolucion || 'Sin notas' }}
                </small>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Modal Resolver -->
      <div class="modal" [class.show]="mostrarModal" [style.display]="mostrarModal ? 'block' : 'none'">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">{{ accionModal === 'resolver' ? 'Resolver' : 'Descartar' }} Alerta</h5>
              <button type="button" class="btn-close" (click)="cerrarModal()"></button>
            </div>
            <div class="modal-body">
              <div class="mb-3">
                <label class="form-label">Tu nombre *</label>
                <input type="text" class="form-control" [(ngModel)]="nombreUsuario">
              </div>
              <div class="mb-3">
                <label class="form-label">Notas / Observaciones *</label>
                <textarea class="form-control" [(ngModel)]="notasResolucion" rows="3"></textarea>
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" (click)="cerrarModal()">Cancelar</button>
              <button type="button" class="btn" 
                      [class.btn-success]="accionModal === 'resolver'"
                      [class.btn-warning]="accionModal === 'descartar'"
                      (click)="confirmarAccion()"
                      [disabled]="!nombreUsuario || !notasResolucion">
                {{ accionModal === 'resolver' ? 'Resolver' : 'Descartar' }}
              </button>
            </div>
          </div>
        </div>
      </div>
      <div class="modal-backdrop fade" [class.show]="mostrarModal" *ngIf="mostrarModal"></div>
    </div>
  `,
  styles: [`
    .modal.show {
      display: block;
      background-color: rgba(0,0,0,0.5);
    }
  `]
})
export class AlertasListComponent implements OnInit {
  
  alertas: Alerta[] = [];
  cargando = false;
  filtroActivo: 'todas' | 'activas' | 'criticas' = 'activas';

  // Modal
  mostrarModal = false;
  accionModal: 'resolver' | 'descartar' = 'resolver';
  alertaSeleccionada: Alerta | null = null;
  nombreUsuario = '';
  notasResolucion = '';

  constructor(private alertaService: AlertaService) { }

  ngOnInit(): void {
    this.cargarAlertas();
  }

  filtrar(tipo: 'todas' | 'activas' | 'criticas'): void {
    this.filtroActivo = tipo;
    this.cargarAlertas();
  }

  cargarAlertas(): void {
    this.cargando = true;
    
    let observable;
    switch (this.filtroActivo) {
      case 'todas':
        observable = this.alertaService.obtenerTodas();
        break;
      case 'criticas':
        observable = this.alertaService.obtenerCriticas();
        break;
      default:
        observable = this.alertaService.obtenerActivas();
    }

    observable.subscribe({
      next: (response) => {
        this.alertas = response.data;
        this.cargando = false;
      },
      error: (error) => {
        console.error('Error al cargar alertas:', error);
        this.cargando = false;
      }
    });
  }

  mostrarModalResolver(alerta: Alerta): void {
    this.alertaSeleccionada = alerta;
    this.accionModal = 'resolver';
    this.mostrarModal = true;
  }

  mostrarModalDescartar(alerta: Alerta): void {
    this.alertaSeleccionada = alerta;
    this.accionModal = 'descartar';
    this.mostrarModal = true;
  }

  confirmarAccion(): void {
    if (!this.alertaSeleccionada || !this.nombreUsuario || !this.notasResolucion) {
      return;
    }

    const observable = this.accionModal === 'resolver'
      ? this.alertaService.resolver(this.alertaSeleccionada.id!, this.nombreUsuario, this.notasResolucion)
      : this.alertaService.descartar(this.alertaSeleccionada.id!, this.nombreUsuario, this.notasResolucion);

    observable.subscribe({
      next: (response) => {
        console.log('Alerta actualizada exitosamente');
        this.cerrarModal();
        this.cargarAlertas();
      },
      error: (error) => {
        console.error('Error al actualizar alerta:', error);
        alert('Error al procesar la acción');
      }
    });
  }

  cerrarModal(): void {
    this.mostrarModal = false;
    this.alertaSeleccionada = null;
    this.nombreUsuario = '';
    this.notasResolucion = '';
  }

  getSeveridadClass(severidad: string): string {
    const classes: Record<string, string> = {
      'CRÍTICA': 'danger',
      'MODERADA': 'warning',
      'BAJA': 'info'
    };
    return classes[severidad] || 'secondary';
  }
}
