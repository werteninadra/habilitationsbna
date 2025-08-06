import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { catchError, Observable, throwError } from 'rxjs';
import { Ressource } from '../models/ressource';

@Injectable({
  providedIn: 'root'
})
export class RessourceService {
    private apiUrl = 'http://localhost:8081/api/ressources';

  constructor(private http: HttpClient) { }

  getAllRessources(): Observable<Ressource[]> {
    return this.http.get<Ressource[]>(this.apiUrl, {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      })
    }).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse) {
    console.error('Erreur:', error);
    return throwError(() => new Error('Une erreur est survenue'));
  }
getAllRessourcesWithProfils(): Observable<Ressource[]> {
    return this.http.get<Ressource[]>(`${this.apiUrl}/with-profils`);
}
  getRessourceById(code: string): Observable<Ressource> {
    return this.http.get<Ressource>(`${this.apiUrl}/${code}`);
  }

  createRessource(ressource: Ressource): Observable<Ressource> {
    return this.http.post<Ressource>(this.apiUrl, ressource);
  }

  updateRessource(code: string, ressource: Ressource): Observable<Ressource> {
    return this.http.put<Ressource>(`${this.apiUrl}/${code}`, ressource);
  }

  deleteRessource(code: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${code}`);
  }

  assignToProfil(codeRessource: string, codeProfil: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${codeRessource}/assign/${codeProfil}`, {});
  }
}