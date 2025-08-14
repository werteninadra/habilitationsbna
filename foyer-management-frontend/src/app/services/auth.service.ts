import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { KeycloakService } from '../keycloak.service';
import { JwtHelperService } from '@auth0/angular-jwt';
@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = 'http://localhost:8081/api/auth';

  constructor(
    private http: HttpClient,
    private keycloakService: KeycloakService
  ) {}
// Dans auth.service.ts
  login(credentials: { matricule: string; password: string }): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, credentials).pipe(
      tap((response: any) => {
        if (response?.token) {
          localStorage.setItem('token', response.token);
          localStorage.setItem('matricule', credentials.matricule);
        }
      }),
      catchError(error => {
        let errorMsg = 'Erreur de connexion';
        if (error.status === 401) {
          errorMsg = 'Matricule ou mot de passe incorrect';
        } else if (error.error?.message) {
          errorMsg = error.error.message;
        }
        return throwError(() => new Error(errorMsg));
      })
    );
  }

forgotPassword(email: string): Observable<any> {
  return this.http.post(`${this.apiUrl}/forgot-password`, { email }).pipe(
    catchError(error => {
      return throwError(() => this.handleError(error));
    })
  );
}

resetPassword(token: string, newPassword: string): Observable<any> {
  return this.http.post(`${this.apiUrl}/reset-password`, { 
    token, 
    newPassword 
  }).pipe(
    catchError(error => {
      return throwError(() => this.handleError(error));
    })
  );
}

private handleError(error: any): string {
  if (error.error?.error) {
    return error.error.error;
  }
  if (error.error?.message) {
    return error.error.message;
  }
  return 'Une erreur inconnue est survenue';
}
  // ... other methods ...

  logout(): Observable<any> {
    const matricule = this.keycloakService.getMatricule();
    
    if (!matricule) {
      this.keycloakService.logout();
      return throwError(() => new Error('Matricule non disponible'));
    }

    const token = this.keycloakService.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    return this.http.post(
      `${this.apiUrl}/logout`, 
      { matricule },
      { headers }
    ).pipe(
      tap(() => {
        this.keycloakService.logout();
      }),
      catchError(error => {
        this.keycloakService.logout();
        return throwError(() => error);
      })
    );
  }
 
  getCurrentUser(): Observable<any> {
    const token = this.getToken();
    if (!token) {
      return throwError(() => new Error('No token found'));
    }

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    return this.http.get(`${this.apiUrl}/me`, { headers }).pipe(
      tap((user: any) => {
        if (user && !user.active) {
          console.warn('User is not active despite being authenticated');
        }
      }),
      catchError(error => {
        if (error.status === 401) {
          this.clearAuthData();
        }
        return throwError(() => error);
      })
    );
  }
  private getCurrentMatricule(): string | null {
  // Implement logic to get matricule from:
  // 1. Current user in state/store
  // 2. Decoded JWT token
  // 3. Or other source
  return localStorage.getItem('matricule'); // Example
}
// In auth.service.ts

// Add this method to decode matricule from JWT
private decodeMatriculeFromToken(token: string): string | null {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.preferred_username || payload.sub || null;
  } catch {
    return null;
  }
}

// Update logout

public clearLocalData(): void {
  localStorage.removeItem('token');
  localStorage.removeItem('matricule');
}
  register(user: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, user);
  }

  getAllUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/users`);
  }

  getUserByMatricule(matricule: string): Observable<any> {
    if (!matricule) {
      return throwError(() => new Error('Matricule non fourni'));
    }
    return this.http.get(`${this.apiUrl}/users/matricule/${matricule}`);
  }

  getUserById(id: string): Observable<any> {
    const token = this.getToken();
    const headers = new HttpHeaders(
      token ? { 'Authorization': `Bearer ${token}` } : {}
    );

    return this.http.get<any>(`${this.apiUrl}/users/${id}`, { headers });
  }

  updateUser(matricule: string, userData: any): Observable<any> {
    const token = this.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    return this.http.put(`${this.apiUrl}/update/${matricule}`, userData, { headers });
  }

  deleteUser(matricule: string): Observable<any> {
    const token = this.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    return this.http.delete(`${this.apiUrl}/users/${matricule}`, { headers });
  }

  getAllUsersWithDetails(): Observable<any[]> {
    const token = this.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    return this.http.get<any[]>(`${this.apiUrl}/users-with-details`, { headers });
  }

  toggleUserStatus(matricule: string, action: string): Observable<any> {
    const token = this.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    const url = `${this.apiUrl}/users/${matricule}/${action}`;
    return this.http.put(url, {}, { headers });
  }

  uploadProfileImage(matricule: string, formData: FormData): Observable<any> {
    const token = this.getToken();
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    return this.http.post(
      `${this.apiUrl}/users/${matricule}/upload-image`, 
      formData,
      { headers }
    );
  }

  private getToken(): string | null {
    return localStorage.getItem('token');
  }

  private clearAuthData(): void {
    localStorage.removeItem('token');
  }





  // auth.service.ts

// Dans la classe AuthService
private jwtHelper = new JwtHelperService();

getDecodedToken(): any {
  const token = this.getToken();
  return token ? this.jwtHelper.decodeToken(token) : null;
}

hasRole(requiredRole: string): boolean {
  const decodedToken = this.getDecodedToken();
  if (!decodedToken) return false;

  // Vérifie dans realm_access.roles (Keycloak standard)
  const realmRoles = decodedToken.realm_access?.roles || [];
  
  // Vérifie aussi dans les claims directs
  const directRoles = decodedToken.roles || [];

  // Combine les deux sources de rôles
  const allRoles = [...realmRoles, ...directRoles];

  return allRoles.some(role => 
    role.toUpperCase() === requiredRole.toUpperCase()
  );
}

hasAnyRole(requiredRoles: string[]): boolean {
  return requiredRoles.some(role => this.hasRole(role));
}

getUserRoles(): string[] {
  const decodedToken = this.getDecodedToken();
  if (!decodedToken) return [];

  return [
    ...(decodedToken.realm_access?.roles || []),
    ...(decodedToken.roles || [])
  ];
}
}