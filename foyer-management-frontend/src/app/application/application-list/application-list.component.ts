import { Component, OnInit } from '@angular/core';
import { ApplicationService } from '../../services/application.service';
import { Application } from '../../models/Application';
import { JiraService } from '../../services/JiraService';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NavbarComponent } from '../../navbar/navbar.component';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-application-list',
  standalone: true,
  imports: [CommonModule, RouterModule, NavbarComponent],
  templateUrl: './application-list.component.html',
  styleUrls: ['./application-list.component.css']
})
export class ApplicationListComponent implements OnInit {
  applications: Application[] = [];
  filteredApplications: Application[] = [];
  ticketsMap: { [code: string]: any[] } = {};
  isLoading = true;
  error: string | null = null;
  jiraDomain = 'nadrawertani22';  // <-- Déclare ici ton sous-domaine Jira

  constructor(
    private applicationService: ApplicationService,
    private jiraService: JiraService,
         public authService: AuthService,
    
  ) {}

  ngOnInit(): void {
    this.loadApplications();
  }

  loadApplications(): void {
    this.isLoading = true;
    this.error = null;

    this.applicationService.getAll().subscribe({
      next: (data) => {
        this.applications = data;
        this.filteredApplications = data;
        this.isLoading = false;

        // Pour chaque application, appeler Jira et stocker les tickets
        this.applications.forEach(app => {
          this.jiraService.getTicketsForProject(app.code).subscribe({
            next: (ticketsData) => {
              this.ticketsMap[app.code] = ticketsData?.issues || [];
            },
            error: (err) => {
              console.error(`Erreur Jira pour ${app.code}`, err);
              // Optionnel : afficher erreur utilisateur, ou stocker dans ticketsMap une erreur
              this.ticketsMap[app.code] = [];
            }
          });
        });
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des applications';
        this.isLoading = false;
      }
    });
  }

  deleteApplication(code: string): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette application ?')) {
      this.applicationService.delete(code).subscribe({
        next: () => {
          this.applications = this.applications.filter(app => app.code !== code);
          this.filteredApplications = this.filteredApplications.filter(app => app.code !== code);
          delete this.ticketsMap[code];
        },
        error: () => {
          this.error = 'Erreur lors de la suppression';
        }
      });
    }
  }

  onSearch(term: string): void {
    if (!term) {
      this.filteredApplications = this.applications;
    } else {
      const lowerTerm = term.toLowerCase();
      this.filteredApplications = this.applications.filter(app =>
        app.code.toLowerCase().includes(lowerTerm) ||
        (app.description && app.description.toLowerCase().includes(lowerTerm)) ||
        (app.libelle && app.libelle.toLowerCase().includes(lowerTerm))
      );
    }
  }
}
