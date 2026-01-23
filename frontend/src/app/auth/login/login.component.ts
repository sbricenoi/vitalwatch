import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

/**
 * Componente de Login
 * Maneja la autenticación de usuarios del sistema VitalWatch
 */
@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  loading = false;
  error = '';
  returnUrl = '/dashboard';

  // Credenciales de prueba para mostrar en la UI
  testCredentials = [
    { tipo: 'Admin', email: 'admin@vitalwatch.com', password: 'Admin123!' },
    { tipo: 'Médico', email: 'medico@vitalwatch.com', password: 'Medico123!' },
    { tipo: 'Enfermera', email: 'enfermera@vitalwatch.com', password: 'Enfermera123!' }
  ];

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    // Redirigir si ya está autenticado
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
    }
  }

  ngOnInit(): void {
    // Crear formulario de login
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });

    // Obtener URL de retorno desde query params
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';
  }

  /**
   * Obtiene el control del formulario por nombre
   */
  get f() {
    return this.loginForm.controls;
  }

  /**
   * Maneja el submit del formulario de login
   */
  onSubmit(): void {
    // Validar formulario
    if (this.loginForm.invalid) {
      this.markFormGroupTouched(this.loginForm);
      return;
    }

    this.loading = true;
    this.error = '';

    const { email, password } = this.loginForm.value;

    this.authService.login(email, password).subscribe({
      next: (response) => {
        console.log('Login exitoso:', response);
        
        // Redirigir al dashboard o a la URL de retorno
        this.router.navigate([this.returnUrl]);
      },
      error: (error) => {
        console.error('Error en login:', error);
        
        // Mostrar mensaje de error amigable
        if (error.status === 400 || error.status === 401) {
          this.error = 'Credenciales inválidas. Verifica tu email y contraseña.';
        } else if (error.status === 0) {
          this.error = 'No se pudo conectar con el servidor. Verifica tu conexión.';
        } else {
          this.error = error.error?.message || 'Error al iniciar sesión. Intenta nuevamente.';
        }
        
        this.loading = false;
      },
      complete: () => {
        this.loading = false;
      }
    });
  }

  /**
   * Rellena el formulario con credenciales de prueba
   * @param credential Credencial de prueba
   */
  fillCredentials(credential: any): void {
    this.loginForm.patchValue({
      email: credential.email,
      password: credential.password
    });
  }

  /**
   * Marca todos los controles del formulario como touched
   * para mostrar errores de validación
   */
  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();

      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }
}
