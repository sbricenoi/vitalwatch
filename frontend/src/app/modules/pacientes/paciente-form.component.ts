import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PacienteService } from '../../shared/services/paciente.service';
import { Paciente } from '../../models/paciente.model';

/**
 * Componente para crear/editar pacientes
 */
@Component({
  selector: 'app-paciente-form',
  template: `
    <div class="container">
      <div class="row justify-content-center">
        <div class="col-md-8">
          <div class="card">
            <div class="card-header bg-primary text-white">
              <h4 class="mb-0">{{ esEdicion ? 'Editar Paciente' : 'Nuevo Paciente' }}</h4>
            </div>
            <div class="card-body">
              <form [formGroup]="pacienteForm" (ngSubmit)="onSubmit()">
                
                <!-- Datos Personales -->
                <h5 class="mb-3">Datos Personales</h5>
                <div class="row">
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Nombre *</label>
                    <input type="text" class="form-control" formControlName="nombre"
                           [class.is-invalid]="submitted && f['nombre'].errors">
                    <div class="invalid-feedback" *ngIf="submitted && f['nombre'].errors">
                      El nombre es requerido (2-100 caracteres)
                    </div>
                  </div>
                  
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Apellido *</label>
                    <input type="text" class="form-control" formControlName="apellido"
                           [class.is-invalid]="submitted && f['apellido'].errors">
                    <div class="invalid-feedback" *ngIf="submitted && f['apellido'].errors">
                      El apellido es requerido (2-100 caracteres)
                    </div>
                  </div>
                </div>

                <div class="row">
                  <div class="col-md-6 mb-3">
                    <label class="form-label">RUT *</label>
                    <input type="text" class="form-control" formControlName="rut"
                           placeholder="12345678-9"
                           [class.is-invalid]="submitted && f['rut'].errors">
                    <small class="text-muted">Formato: 12345678-9</small>
                    <div class="invalid-feedback" *ngIf="submitted && f['rut'].errors">
                      RUT requerido (formato: 12345678-9 o 12345678-K)
                    </div>
                  </div>
                  
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Género *</label>
                    <select class="form-select" formControlName="genero"
                            [class.is-invalid]="submitted && f['genero'].errors">
                      <option value="">Seleccionar...</option>
                      <option value="M">Masculino</option>
                      <option value="F">Femenino</option>
                      <option value="O">Otro</option>
                    </select>
                    <div class="invalid-feedback" *ngIf="submitted && f['genero'].errors">
                      El género es requerido
                    </div>
                  </div>
                </div>

                <div class="row">
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Fecha de Nacimiento *</label>
                    <input type="date" class="form-control" formControlName="fechaNacimiento"
                           [class.is-invalid]="submitted && f['fechaNacimiento'].errors">
                    <div class="invalid-feedback" *ngIf="submitted && f['fechaNacimiento'].errors">
                      La fecha de nacimiento es requerida
                    </div>
                  </div>
                  
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Edad *</label>
                    <input type="number" class="form-control" formControlName="edad"
                           [class.is-invalid]="submitted && f['edad'].errors" readonly>
                    <small class="text-muted">Se calcula automáticamente</small>
                    <div class="invalid-feedback" *ngIf="submitted && f['edad'].errors">
                      La edad es requerida (0-120)
                    </div>
                  </div>
                </div>

                <!-- Datos Hospitalización -->
                <h5 class="mb-3 mt-4">Datos de Hospitalización</h5>
                <div class="row">
                  <div class="col-md-4 mb-3">
                    <label class="form-label">Sala *</label>
                    <input type="text" class="form-control" formControlName="sala"
                           placeholder="Ej: UCI-201"
                           [class.is-invalid]="submitted && f['sala'].errors">
                    <div class="invalid-feedback" *ngIf="submitted && f['sala'].errors">
                      La sala es requerida (máx. 20 caracteres)
                    </div>
                  </div>
                  
                  <div class="col-md-4 mb-3">
                    <label class="form-label">Cama *</label>
                    <input type="text" class="form-control" formControlName="cama"
                           placeholder="Ej: A-12"
                           [class.is-invalid]="submitted && f['cama'].errors">
                    <div class="invalid-feedback" *ngIf="submitted && f['cama'].errors">
                      La cama es requerida (máx. 10 caracteres)
                    </div>
                  </div>
                  
                  <div class="col-md-4 mb-3">
                    <label class="form-label">Estado *</label>
                    <select class="form-select" formControlName="estado"
                            [class.is-invalid]="submitted && f['estado'].errors">
                      <option value="ESTABLE">Estable</option>
                      <option value="MODERADO">Moderado</option>
                      <option value="CRÍTICO">Crítico</option>
                      <option value="RECUPERACIÓN">Recuperación</option>
                    </select>
                    <div class="invalid-feedback" *ngIf="submitted && f['estado'].errors">
                      El estado es requerido
                    </div>
                  </div>
                </div>

                <div class="mb-3">
                  <label class="form-label">Diagnóstico</label>
                  <textarea class="form-control" formControlName="diagnostico" rows="3"
                            placeholder="Diagnóstico médico del paciente (máx. 500 caracteres)"></textarea>
                  <small class="text-muted">Opcional - Máximo 500 caracteres</small>
                </div>

                <!-- Botones -->
                <div class="d-flex justify-content-between mt-4">
                  <button type="button" class="btn btn-secondary" (click)="volver()">
                    Cancelar
                  </button>
                  <button type="submit" class="btn btn-primary" [disabled]="guardando">
                    <span *ngIf="guardando" class="spinner-border spinner-border-sm me-2"></span>
                    {{ esEdicion ? 'Actualizar' : 'Guardar' }}
                  </button>
                </div>
              </form>

              <!-- Mensajes -->
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
    .form-label {
      font-weight: 600;
    }
    
    h5 {
      color: #2563eb;
      border-bottom: 2px solid #e5e7eb;
      padding-bottom: 0.5rem;
    }
  `]
})
export class PacienteFormComponent implements OnInit {

  pacienteForm!: FormGroup;
  submitted = false;
  guardando = false;
  esEdicion = false;
  pacienteId?: number;
  mensaje = '';
  error = false;

  constructor(
    private fb: FormBuilder,
    private pacienteService: PacienteService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.inicializarFormulario();
    
    // Verificar si es edición
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.esEdicion = true;
        this.pacienteId = +params['id'];
        this.cargarPaciente();
      }
    });
  }

  inicializarFormulario(): void {
    this.pacienteForm = this.fb.group({
      nombre: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      apellido: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      rut: ['', [Validators.required, Validators.pattern(/^\d{7,8}-[\dkK]$/)]],
      fechaNacimiento: ['', Validators.required],
      edad: ['', [Validators.required, Validators.min(0), Validators.max(120)]],
      genero: ['', Validators.required],
      sala: ['', [Validators.required, Validators.maxLength(20)]],
      cama: ['', [Validators.required, Validators.maxLength(10)]],
      estado: ['ESTABLE', Validators.required],
      diagnostico: ['', Validators.maxLength(500)]
    });

    // Calcular edad automáticamente cuando cambia la fecha de nacimiento
    this.pacienteForm.get('fechaNacimiento')?.valueChanges.subscribe(fecha => {
      if (fecha) {
        const edad = this.calcularEdad(fecha);
        this.pacienteForm.patchValue({ edad }, { emitEvent: false });
      }
    });
  }

  /**
   * Calcula la edad a partir de una fecha de nacimiento
   */
  calcularEdad(fechaNacimiento: string): number {
    const hoy = new Date();
    const nacimiento = new Date(fechaNacimiento);
    let edad = hoy.getFullYear() - nacimiento.getFullYear();
    const mes = hoy.getMonth() - nacimiento.getMonth();
    
    // Ajustar si aún no ha cumplido años este año
    if (mes < 0 || (mes === 0 && hoy.getDate() < nacimiento.getDate())) {
      edad--;
    }
    
    return edad >= 0 ? edad : 0;
  }

  get f() {
    return this.pacienteForm.controls;
  }

  cargarPaciente(): void {
    if (!this.pacienteId) return;
    
    this.pacienteService.obtenerPorId(this.pacienteId).subscribe({
      next: (response) => {
        this.pacienteForm.patchValue(response.data);
      },
      error: (error) => {
        console.error('Error al cargar paciente:', error);
        this.mostrarMensaje('Error al cargar los datos del paciente', true);
      }
    });
  }

  onSubmit(): void {
    this.submitted = true;

    if (this.pacienteForm.invalid) {
      return;
    }

    this.guardando = true;
    const paciente: Paciente = this.pacienteForm.value;

    const operacion = this.esEdicion 
      ? this.pacienteService.actualizar(this.pacienteId!, paciente)
      : this.pacienteService.crear(paciente);

    operacion.subscribe({
      next: (response) => {
        this.mostrarMensaje(
          this.esEdicion ? 'Paciente actualizado exitosamente' : 'Paciente creado exitosamente',
          false
        );
        setTimeout(() => this.volver(), 2000);
      },
      error: (error) => {
        console.error('Error al guardar paciente:', error);
        this.mostrarMensaje('Error al guardar el paciente', true);
        this.guardando = false;
      }
    });
  }

  mostrarMensaje(mensaje: string, error: boolean): void {
    this.mensaje = mensaje;
    this.error = error;
  }

  volver(): void {
    this.router.navigate(['/pacientes']);
  }
}
