import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AlertasListComponent } from './alertas-list.component';

const routes: Routes = [
  {
    path: '',
    component: AlertasListComponent
  }
];

@NgModule({
  declarations: [
    AlertasListComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule.forChild(routes)
  ]
})
export class AlertasModule { }
