
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Agence {
  id?: number;  // <-- rendre optionnel
  nom: string;
  capaciteMax: number;
  adresse?: string; 
    latitude?: number;   // <-- Ajoute ces propriétés
  longitude?: number;  /// Optionnel si le backend ne le renvoie pas
}
export interface PredictionResponse {
  prediction: number[];
}

export interface PredictionMessage {
  message: string;
}
export interface Occupation {
  id?: number;
  date?: string;
  nombreClients: number;
  estFerie: boolean;
  meteo: string;
  jourSemaine: number;
  agenceId: number;
  tauxOccupation?: number; // Optionnel si le backend ne le renvoie pas
}

@Injectable({
  providedIn: 'root'
})
export class AgenceService {
  private baseUrl = 'http://localhost:8081/api';

  constructor(private http: HttpClient) {}

  getAgences(): Observable<Agence[]> {
   const token = localStorage.getItem('token');
return this.http.get<Agence[]>(`${this.baseUrl}/agences`, {
  headers: { Authorization: `Bearer ${token}` }
});

  }
 getOccupations(agenceId: number): Observable<Occupation[]> {
  return this.http.get<Occupation[]>(`${this.baseUrl}/occupations/details/${agenceId}`);
}
getAgence(id: number): Observable<Agence> {
    return this.http.get<Agence>(`${this.baseUrl}/agences/${id}`);
  }

 getPredictionToday(agenceId: number): Observable<PredictionMessage> {
  return this.http.get<PredictionMessage>(`${this.baseUrl}/occupations/prediction/today/${agenceId}`);
}
  createOccupation(occupation: Occupation): Observable<Occupation> {
  return this.http.post<Occupation>(
    `${this.baseUrl}/occupations/create/${occupation.agenceId}`,
    null,
    {
      params: {
        nombreClients: occupation.nombreClients.toString(),
        estFerie: occupation.estFerie.toString(),
        meteo: occupation.meteo,
        jourSemaine: occupation.jourSemaine.toString()
      }
    }
  );
}
updateAgence(agence: Agence): Observable<Agence> {
    return this.http.put<Agence>(`${this.baseUrl}/agences/${agence.id}`, agence);
  }
getOccupationById(id: number): Observable<Occupation> {
  return this.http.get<Occupation>(`${this.baseUrl}/occupations/${id}`);
}
downloadPredictionPDF(agenceId: number) {
  return this.http.get(`/api/occupations/prediction/pdf/${agenceId}`, {
    responseType: 'blob'
  });
}

  deleteAgence(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/agences/${id}`);
  }
  updateOccupation(occupation: Occupation): Observable<Occupation> {
    return this.http.put<Occupation>(`${this.baseUrl}/occupations/${occupation.id}`, occupation);
  }

  deleteOccupation(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/occupations/${id}`);
  }
createAgence(agence: Agence): Observable<Agence> {
  return this.http.post<Agence>(`${this.baseUrl}/agences`, agence);
}



}
