import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

/**
 * Servicio base para realizar peticiones HTTP a la API
 */
@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  /**
   * Obtener headers por defecto
   */
  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    });
  }

  /**
   * Realizar petición GET
   */
  get<T>(path: string): Observable<T> {
    return this.http.get<T>(`${this.apiUrl}/${path}`, {
      headers: this.getHeaders()
    });
  }

  /**
   * Realizar petición POST
   */
  post<T>(path: string, body: any): Observable<T> {
    return this.http.post<T>(`${this.apiUrl}/${path}`, body, {
      headers: this.getHeaders()
    });
  }

  /**
   * Realizar petición PUT
   */
  put<T>(path: string, body: any): Observable<T> {
    return this.http.put<T>(`${this.apiUrl}/${path}`, body, {
      headers: this.getHeaders()
    });
  }

  /**
   * Realizar petición DELETE
   */
  delete<T>(path: string): Observable<T> {
    return this.http.delete<T>(`${this.apiUrl}/${path}`, {
      headers: this.getHeaders()
    });
  }
}
