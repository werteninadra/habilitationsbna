import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class JiraService {
  private baseUrl = 'http://localhost:8081/api/jira/issues';

  constructor(private http: HttpClient) {}

  getTicketsForProject(projectKey: string): Observable<any> {
    return this.http.get(`${this.baseUrl}?project=${projectKey}`);
  }
}
