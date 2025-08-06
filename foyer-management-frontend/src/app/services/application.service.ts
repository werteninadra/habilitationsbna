import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, Observable } from 'rxjs';
import { Application } from '../models/Application';

@Injectable({
  providedIn: 'root'
})
export class ApplicationService {
  private apiUrl = 'http://localhost:8081/api/applications';

  constructor(private http: HttpClient) { }

  getAll(): Observable<Application[]> {
    return this.http.get<Application[]>(this.apiUrl);
  }

  getByCode(code: string): Observable<Application> {
    return this.http.get<Application>(`${this.apiUrl}/${code}`);
  }

  
create(application: Application): Observable<Application> {
    return this.http.post<Application>(this.apiUrl, application).pipe(
        catchError(error => {
            console.error('Erreur création application:', error);
            throw error;
        })
    );
}
  update(code: string, application: Application): Observable<Application> {
    return this.http.put<Application>(`${this.apiUrl}/${code}`, application);
  }

  delete(code: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${code}`);
  }
}