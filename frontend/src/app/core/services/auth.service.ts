import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

/**
 * Interfaz para la solicitud de login
 */
export interface LoginRequest {
  email: string;
  password: string;
}

/**
 * Interfaz para la respuesta de login
 */
export interface LoginResponse {
  token: string;
  tipo: string;
  id: number;
  nombre: string;
  email: string;
  rol: string;
}

/**
 * Interfaz genérica para respuestas de API
 */
export interface ApiResponse<T> {
  traceId: string;
  code: string;
  message: string;
  data: T;
}

/**
 * Servicio de Autenticación
 * Maneja el login, logout y estado de autenticación del usuario
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject: BehaviorSubject<LoginResponse | null>;
  public currentUser: Observable<LoginResponse | null>;

  constructor(private http: HttpClient) {
    // Cargar usuario desde localStorage al inicializar
    const storedUser = localStorage.getItem('currentUser');
    this.currentUserSubject = new BehaviorSubject<LoginResponse | null>(
      storedUser ? JSON.parse(storedUser) : null
    );
    this.currentUser = this.currentUserSubject.asObservable();
  }

  /**
   * Obtiene el valor actual del usuario autenticado
   */
  public get currentUserValue(): LoginResponse | null {
    return this.currentUserSubject.value;
  }

  /**
   * Realiza el login del usuario
   * @param email Email del usuario
   * @param password Contraseña del usuario
   * @returns Observable con la respuesta del login
   */
  login(email: string, password: string): Observable<ApiResponse<LoginResponse>> {
    return this.http.post<ApiResponse<LoginResponse>>(
      `${environment.apiUrl}/auth/login`,
      { email, password }
    ).pipe(
      tap(response => {
        if (response.data) {
          // Guardar usuario y token en localStorage
          localStorage.setItem('currentUser', JSON.stringify(response.data));
          localStorage.setItem('token', response.data.token);
          
          // Actualizar el subject
          this.currentUserSubject.next(response.data);
          
          console.log('Login exitoso:', response.data.nombre);
        }
      })
    );
  }

  /**
   * Cierra la sesión del usuario
   */
  logout(): void {
    // Limpiar localStorage
    localStorage.removeItem('currentUser');
    localStorage.removeItem('token');
    
    // Actualizar el subject
    this.currentUserSubject.next(null);
    
    console.log('Sesión cerrada');
  }

  /**
   * Verifica si el usuario está autenticado
   * @returns true si hay un usuario autenticado
   */
  isAuthenticated(): boolean {
    return !!this.currentUserValue;
  }

  /**
   * Obtiene el token del usuario actual
   * @returns Token JWT o null
   */
  getToken(): string | null {
    return localStorage.getItem('token');
  }

  /**
   * Obtiene el rol del usuario actual
   * @returns Rol del usuario o null
   */
  getUserRole(): string | null {
    return this.currentUserValue?.rol || null;
  }

  /**
   * Obtiene el nombre del usuario actual
   * @returns Nombre del usuario o null
   */
  getUserName(): string | null {
    return this.currentUserValue?.nombre || null;
  }

  /**
   * Obtiene el email del usuario actual
   * @returns Email del usuario o null
   */
  getUserEmail(): string | null {
    return this.currentUserValue?.email || null;
  }

  /**
   * Verifica si el usuario tiene un rol específico
   * @param rol Rol a verificar
   * @returns true si el usuario tiene el rol
   */
  hasRole(rol: string): boolean {
    return this.getUserRole() === rol;
  }

  /**
   * Verifica si el usuario es administrador
   * @returns true si el usuario es ADMIN
   */
  isAdmin(): boolean {
    return this.hasRole('ADMIN');
  }

  /**
   * Verifica si el usuario es médico
   * @returns true si el usuario es MEDICO
   */
  isMedico(): boolean {
    return this.hasRole('MEDICO');
  }

  /**
   * Verifica si el usuario es enfermera
   * @returns true si el usuario es ENFERMERA
   */
  isEnfermera(): boolean {
    return this.hasRole('ENFERMERA');
  }
}
