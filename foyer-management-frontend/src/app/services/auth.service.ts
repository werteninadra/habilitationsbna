import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = 'http://localhost:8081/api/auth';

  constructor(private http: HttpClient) {}

  login(credentials: { matricule: string; password: string }): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, credentials, { withCredentials: true });
  }

  getCurrentUser(): Observable<any> {
    return this.http.get(`${this.apiUrl}/me`, { withCredentials: true });
  }

  register(user: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, user);
  }

  logout(): Observable<any> {
    return this.http.post(`${this.apiUrl}/logout`, {}, { withCredentials: true });
  }

  getAllUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/users`);
  }

  /*getUserByMatricule(matricule: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/users/matricule/${matricule}`);
  }*/

  getToken(): string | null {
    return typeof window !== 'undefined' ? localStorage.getItem('token') : null;
  }

  getUserById(id: string): Observable<any> {
    const token = this.getToken();
    const headers = new HttpHeaders(
      token ? { Authorization: `Bearer ${token}` } : {}
    );

    return this.http.get<any>(`${this.apiUrl}/users/${id}`, { headers });
  }
/*getUserByMatricule(matricule: string): Observable<any> {
  return this.http.get(`${this.apiUrl}/users/matricule/${matricule}`);
}*/
getUserByMatricule(matricule: string): Observable<any> {
  if (!matricule) {
    return throwError(() => new Error('Matricule non fourni'));
  }
  return this.http.get(`${this.apiUrl}/users/matricule/${matricule}`);
}
// Dans auth.service.ts
updateUser(matricule: string, userData: any): Observable<any> {
  return this.http.put(`${this.apiUrl}/update/${matricule}`, userData);
}

deleteUser(matricule: string): Observable<any> {
  return this.http.delete(`${this.apiUrl}/users/${matricule}`);
}
getAllUsersWithDetails(): Observable<any[]> {
  return this.http.get<any[]>(`${this.apiUrl}/users-with-details`);
}
}