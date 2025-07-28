import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { KeycloakService } from '../keycloak.service';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class ProfilService {
  private apiUrl = 'http://localhost:8081/api/profils';

  constructor(
    private http: HttpClient,
    private keycloakService: KeycloakService,
    private router: Router
  ) {}

  private getAuthHeaders(): HttpHeaders {
    const token = this.keycloakService.getToken();
    if (!token) {
      console.error("Token manquant. Redirection...");
      this.keycloakService.logout();
      throw new Error('No token available');
    }

    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  getAllProfils(): Observable<any> {
    return this.http.get(this.apiUrl, { headers: this.getAuthHeaders() }).pipe(
      catchError(err => {
        if (err.status === 401) {
          this.keycloakService.logout();
        }
        return throwError(() => err);
      })
    );
  }

  createProfil(profil: any): Observable<any> {
    return this.http.post(this.apiUrl, profil, { headers: this.getAuthHeaders() }).pipe(
      catchError(err => {
        if (err.status === 401) {
          this.keycloakService.logout();
        }
        return throwError(() => err);
      })
    );
  }
}
