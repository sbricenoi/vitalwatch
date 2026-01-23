import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { SignosVitalesListComponent } from './signos-vitales-list.component';
import { SignosVitalesFormComponent } from './signos-vitales-form.component';

const routes: Routes = [
  {
    path: '',
    component: SignosVitalesListComponent
  },
  {
    path: 'registrar',
    component: SignosVitalesFormComponent
  }
];

@NgModule({
  declarations: [
    SignosVitalesListComponent,
    SignosVitalesFormComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule.forChild(routes)
  ]
})
export class SignosVitalesModule { }
