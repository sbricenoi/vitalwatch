import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { PacientesListComponent } from './pacientes-list.component';
import { PacienteFormComponent } from './paciente-form.component';

const routes: Routes = [
  {
    path: '',
    component: PacientesListComponent
  },
  {
    path: 'nuevo',
    component: PacienteFormComponent
  },
  {
    path: 'editar/:id',
    component: PacienteFormComponent
  }
];

@NgModule({
  declarations: [
    PacientesListComponent,
    PacienteFormComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes)
  ]
})
export class PacientesModule { }
