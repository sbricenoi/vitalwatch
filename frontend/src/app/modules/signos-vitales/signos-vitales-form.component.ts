import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { SignosVitalesService } from '../../shared/services/signos-vitales.service';
import { PacienteService } from '../../shared/services/paciente.service';
import { SignosVitales } from '../../models/signos-vitales.model';
import { Paciente } from '../../models/paciente.model';

/**
 * Componente para registrar signos vitales
 */
@Component({
  selector: 'app-signos-vitales-form',
  template: `
    <div class="container">
      <div class="row justify-content-center">
        <div class="col-md-8">
          <div class="card">
            <div class="card-header bg-info text-white">
              <h4 class="mb-0">📊 Registrar Signos Vitales</h4>
            </div>
            <div class="card-body">
              
              <!-- Selección de Paciente -->
              <div class="mb-4" *ngIf="!pacientePreseleccionado">
                <label class="form-label"><strong>Seleccionar Paciente *</strong></label>
                <select class="form-select" [(ngModel)]="pacienteIdSeleccionado" (change)="onPacienteChange()">
                  <option value="">-- Seleccione un paciente --</option>
                  <option *ngFor="let p of pacientes" [value]="p.id">
                    {{ p.nombre }} {{ p.apellido }} - Sala: {{ p.sala }} Cama: {{ p.cama }}
                  </option>
                </select>
              </div>

              <!-- Info del Paciente -->
              <div *ngIf="pacienteSeleccionado" class="alert alert-info mb-4">
                <strong>Paciente:</strong> {{ pacienteSeleccionado.nombre }} {{ pacienteSeleccionado.apellido }}<br>
                <strong>Estado:</strong> <span [ngClass]="'badge bg-' + getEstadoBadge(pacienteSeleccionado.estado)">
                  {{ pacienteSeleccionado.estado }}
                </span>
              </div>

              <!-- Formulario -->
              <form [formGroup]="signosForm" (ngSubmit)="onSubmit()" *ngIf="pacienteSeleccionado">
                
                <!-- Signos Cardiovasculares -->
                <h5 class="mb-3">💓 Signos Cardiovasculares</h5>
                <div class="row">
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Frecuencia Cardíaca (bpm) *</label>
                    <input type="number" class="form-control" formControlName="frecuenciaCardiaca"
                           [class.is-invalid]="submitted && f['frecuenciaCardiaca'].errors">
                    <small class="text-muted">Normal: 60-100 bpm</small>
                    <div class="invalid-feedback">Requerido (40-200)</div>
                  </div>
                  
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Saturación O₂ (%) *</label>
                    <input type="number" class="form-control" formControlName="saturacionOxigeno"
                           [class.is-invalid]="submitted && f['saturacionOxigeno'].errors">
                    <small class="text-muted">Normal: 95-100%</small>
                    <div class="invalid-feedback">Requerido (0-100)</div>
                  </div>
                </div>

                <!-- Presión Arterial -->
                <h5 class="mb-3 mt-3">🩺 Presión Arterial</h5>
                <div class="row">
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Presión Sistólica (mmHg) *</label>
                    <input type="number" class="form-control" formControlName="presionSistolica"
                           [class.is-invalid]="submitted && f['presionSistolica'].errors">
                    <small class="text-muted">Normal: 90-140 mmHg</small>
                    <div class="invalid-feedback">Requerido (60-250)</div>
                  </div>
                  
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Presión Diastólica (mmHg) *</label>
                    <input type="number" class="form-control" formControlName="presionDiastolica"
                           [class.is-invalid]="submitted && f['presionDiastolica'].errors">
                    <small class="text-muted">Normal: 60-90 mmHg</small>
                    <div class="invalid-feedback">Requerido (40-150)</div>
                  </div>
                </div>

                <!-- Temperatura y Respiración -->
                <h5 class="mb-3 mt-3">🌡️ Temperatura y Respiración</h5>
                <div class="row">
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Temperatura (°C) *</label>
                    <input type="number" step="0.1" class="form-control" formControlName="temperatura"
                           [class.is-invalid]="submitted && f['temperatura'].errors">
                    <small class="text-muted">Normal: 36.5-37.5 °C</small>
                    <div class="invalid-feedback">Requerido (35-42)</div>
                  </div>
                  
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Frecuencia Respiratoria (rpm) *</label>
                    <input type="number" class="form-control" formControlName="frecuenciaRespiratoria"
                           [class.is-invalid]="submitted && f['frecuenciaRespiratoria'].errors">
                    <small class="text-muted">Normal: 12-20 rpm</small>
                    <div class="invalid-feedback">Requerido (8-40)</div>
                  </div>
                </div>

                <!-- Estado de Conciencia -->
                <h5 class="mb-3 mt-3">🧠 Estado Neurológico</h5>
                <div class="row">
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Estado de Conciencia *</label>
                    <select class="form-select" formControlName="estadoConciencia"
                            [class.is-invalid]="submitted && f['estadoConciencia'].errors">
                      <option value="">-- Seleccione --</option>
                      <option value="ALERTA">Alerta (Despierto y orientado)</option>
                      <option value="VERBAL">Responde a estímulos verbales</option>
                      <option value="DOLOR">Responde a dolor</option>
                      <option value="INCONSCIENTE">Inconsciente</option>
                    </select>
                    <div class="invalid-feedback">El estado de conciencia es obligatorio</div>
                  </div>
                  
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Registrado por *</label>
                    <input type="text" class="form-control" formControlName="registradoPor"
                           [class.is-invalid]="submitted && f['registradoPor'].errors"
                           placeholder="Nombre del profesional">
                    <small class="text-muted">Nombre completo del profesional</small>
                    <div class="invalid-feedback">El nombre del registrador es obligatorio</div>
                  </div>
                </div>

                <!-- Observaciones -->
                <div class="mb-3">
                  <label class="form-label">Observaciones</label>
                  <textarea class="form-control" formControlName="observaciones" rows="3"
                            placeholder="Cualquier observación adicional sobre el paciente..."></textarea>
                </div>

                <!-- Alerta -->
                <div class="alert alert-warning" role="alert">
                  <strong>⚠️ Nota:</strong> El sistema generará automáticamente alertas si los valores están 
                  fuera de los rangos normales.
                </div>

                <!-- Botones -->
                <div class="d-flex justify-content-between mt-4">
                  <button type="button" class="btn btn-secondary" (click)="volver()">
                    Cancelar
                  </button>
                  <button type="submit" class="btn btn-info text-white" [disabled]="guardando || !pacienteSeleccionado">
                    <span *ngIf="guardando" class="spinner-border spinner-border-sm me-2"></span>
                    Registrar Signos Vitales
                  </button>
                </div>
              </form>

              <!-- Mensaje -->
              <div *ngIf="mensaje" class="alert mt-3" 
                   [class.alert-success]="!error" 
                   [class.alert-danger]="error">
                {{ mensaje }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    h5 {
      color: #0dcaf0;
      border-bottom: 2px solid #e5e7eb;
      padding-bottom: 0.5rem;
    }
    
    small.text-muted {
      display: block;
      font-size: 0.75rem;
    }
  `]
})
export class SignosVitalesFormComponent implements OnInit {

  signosForm!: FormGroup;
  submitted = false;
  guardando = false;
  mensaje = '';
  error = false;

  pacientes: Paciente[] = [];
  pacienteIdSeleccionado: number | null = null;
  pacienteSeleccionado: Paciente | null = null;
  pacientePreseleccionado = false;

  constructor(
    private fb: FormBuilder,
    private signosVitalesService: SignosVitalesService,
    private pacienteService: PacienteService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.inicializarFormulario();
    this.cargarPacientes();

    // Verificar si viene un paciente preseleccionado
    this.route.queryParams.subscribe(params => {
      if (params['pacienteId']) {
        this.pacienteIdSeleccionado = +params['pacienteId'];
        this.pacientePreseleccionado = true;
        this.onPacienteChange();
      }
    });
  }

  inicializarFormulario(): void {
    this.signosForm = this.fb.group({
      frecuenciaCardiaca: ['', [Validators.required, Validators.min(40), Validators.max(200)]],
      presionSistolica: ['', [Validators.required, Validators.min(60), Validators.max(250)]],
      presionDiastolica: ['', [Validators.required, Validators.min(40), Validators.max(150)]],
      temperatura: ['', [Validators.required, Validators.min(35), Validators.max(42)]],
      saturacionOxigeno: ['', [Validators.required, Validators.min(0), Validators.max(100)]],
      frecuenciaRespiratoria: ['', [Validators.required, Validators.min(8), Validators.max(40)]],
      estadoConciencia: ['', [Validators.required]],
      registradoPor: ['', [Validators.required, Validators.maxLength(100)]],
      observaciones: ['']
    });
  }

  get f() {
    return this.signosForm.controls;
  }

  cargarPacientes(): void {
    this.pacienteService.obtenerTodos().subscribe({
      next: (response) => {
        this.pacientes = response.data;
      },
      error: (error) => {
        console.error('Error al cargar pacientes:', error);
      }
    });
  }

  onPacienteChange(): void {
    if (this.pacienteIdSeleccionado) {
      this.pacienteService.obtenerPorId(this.pacienteIdSeleccionado).subscribe({
        next: (response) => {
          this.pacienteSeleccionado = response.data;
        },
        error: (error) => {
          console.error('Error al cargar paciente:', error);
        }
      });
    }
  }

  onSubmit(): void {
    this.submitted = true;

    if (this.signosForm.invalid || !this.pacienteIdSeleccionado) {
      return;
    }

    this.guardando = true;
    
    const signosVitales: SignosVitales = {
      ...this.signosForm.value,
      pacienteId: this.pacienteIdSeleccionado
    };

    this.signosVitalesService.registrar(signosVitales).subscribe({
      next: (response) => {
        this.mostrarMensaje('✅ Signos vitales registrados exitosamente', false);
        this.signosForm.reset();
        this.submitted = false;
        setTimeout(() => {
          if (this.pacientePreseleccionado) {
            this.volver();
          }
        }, 2000);
        this.guardando = false;
      },
      error: (error) => {
        console.error('Error al registrar signos vitales:', error);
        this.mostrarMensaje('❌ Error al registrar los signos vitales', true);
        this.guardando = false;
      }
    });
  }

  mostrarMensaje(mensaje: string, error: boolean): void {
    this.mensaje = mensaje;
    this.error = error;
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

  volver(): void {
    this.router.navigate(['/signos-vitales']);
  }
}
