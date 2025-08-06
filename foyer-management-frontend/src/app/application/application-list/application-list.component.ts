import { Component, OnInit } from '@angular/core';
import { ApplicationService } from '../../services/application.service';
import { Application } from '../../models/Application';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
// ... autres imports
import { NavbarComponent } from '../../navbar/navbar.component'; // mets le bon chemin

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
  isLoading = true;
  error: string | null = null;

  constructor(private applicationService: ApplicationService) {}

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
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des applications';
        this.isLoading = false;
        console.error('Error loading applications', err);
      }
    });
  }

  deleteApplication(code: string): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette application ?')) {
      this.applicationService.delete(code).subscribe({
        next: () => {
          this.applications = this.applications.filter(app => app.code !== code);
          this.filteredApplications = this.filteredApplications.filter(app => app.code !== code);
        },
        error: (err) => {
          this.error = 'Erreur lors de la suppression';
          console.error('Error deleting application', err);
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
        (app.description && app.description.toLowerCase().includes(lowerTerm))
      );
    }
  }
}
