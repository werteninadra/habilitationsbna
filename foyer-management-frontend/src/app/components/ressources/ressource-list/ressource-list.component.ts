import { Component, OnInit } from '@angular/core';
import { RessourceService } from '../../../services/ressource.service';
import { Ressource } from '../../../models/ressource';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from '../../../navbar/navbar.component'; // Ajuste le chemin

@Component({
  selector: 'app-ressource-list',
  standalone: true,
  imports: [CommonModule, RouterModule, NavbarComponent],
  templateUrl: './ressource-list.component.html',
  styleUrls: ['./ressource-list.component.css']
})
export class RessourceListComponent implements OnInit {
  ressources: Ressource[] = [];
  filteredRessources: Ressource[] = [];
  error: string | null = null;

  constructor(private ressourceService: RessourceService) { }

  ngOnInit(): void {
    this.loadRessources();
  }

  loadRessources(): void {
    this.ressourceService.getAllRessourcesWithProfils().subscribe({
      next: (data) => {
        this.ressources = data;
        this.filteredRessources = data;
      },
      error: (err) => {
        console.error('Erreur complète:', err);
        this.error = 'Échec du chargement des ressources';
      }
    });
  }

  deleteRessource(code: string): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette ressource ?')) {
      this.ressourceService.deleteRessource(code).subscribe(
        () => this.loadRessources(),
        error => console.error('Error deleting ressource', error)
      );
    }
  }

  onSearch(term: string): void {
    if (!term) {
      this.filteredRessources = this.ressources;
    } else {
      const lowerTerm = term.toLowerCase();
      this.filteredRessources = this.ressources.filter(r =>
        r.code.toLowerCase().includes(lowerTerm) ||
        r.libelle.toLowerCase().includes(lowerTerm) ||
        r.typeRessource.toLowerCase().includes(lowerTerm) ||
        (r.application && (r.application.code.toLowerCase().includes(lowerTerm) || r.application.libelle.toLowerCase().includes(lowerTerm)))
      );
    }
  }
}
