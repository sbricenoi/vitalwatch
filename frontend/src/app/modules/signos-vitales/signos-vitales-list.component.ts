import { Component } from '@angular/core';

@Component({
  selector: 'app-signos-vitales-list',
  template: `
    <div class="container">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2 class="mb-0">📊 Signos Vitales</h2>
        <button class="btn btn-info text-white" routerLink="/signos-vitales/registrar">
          <i class="bi bi-plus-circle"></i> Registrar Signos Vitales
        </button>
      </div>
      
      <div class="alert alert-info">
        <h5>💡 Información</h5>
        <p>En esta sección puedes registrar y consultar los signos vitales de los pacientes.</p>
        <p><strong>Rangos Normales:</strong></p>
        <ul class="mb-0">
          <li>Frecuencia Cardíaca: 60-100 bpm</li>
          <li>Presión Arterial: 90-140 / 60-90 mmHg</li>
          <li>Temperatura: 36.5-37.5 °C</li>
          <li>Saturación O₂: 95-100%</li>
          <li>Frecuencia Respiratoria: 12-20 rpm</li>
          <li>Glucosa: 70-140 mg/dL</li>
        </ul>
      </div>

      <div class="alert alert-warning">
        <strong>⚠️ Sistema de Alertas Automático:</strong> El sistema genera alertas automáticamente 
        cuando los valores están fuera de los rangos normales.
      </div>
    </div>
  `
})
export class SignosVitalesListComponent { }
