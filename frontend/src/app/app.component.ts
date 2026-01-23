import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './core/services/auth.service';

/**
 * Componente raíz de la aplicación
 */
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  title = 'VitalWatch - Sistema de Alertas Médicas';
  isAuthenticated = false;
  userName: string | null = null;
  userEmail: string | null = null;
  userRole: string | null = null;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Suscribirse a cambios en el estado de autenticación
    this.authService.currentUser.subscribe(user => {
      this.isAuthenticated = !!user;
      this.userName = user?.nombre || null;
      this.userEmail = user?.email || null;
      this.userRole = user?.rol || null;
    });
  }

  /**
   * Cierra la sesión del usuario
   */
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
