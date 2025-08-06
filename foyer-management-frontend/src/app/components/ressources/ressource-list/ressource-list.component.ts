import { Component, OnInit } from '@angular/core';
import { RessourceService } from '../../../services/ressource.service';
import { Ressource } from '../../../models/ressource';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-ressource-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './ressource-list.component.html',
  styleUrls: ['./ressource-list.component.css']
})
export class RessourceListComponent implements OnInit {
  ressources: Ressource[] = [];
  error: string | null = null;

  constructor(private ressourceService: RessourceService) { }

  ngOnInit(): void {
    this.loadRessources();
  }

  loadRessources(): void {
    this.ressourceService.getAllRessourcesWithProfils().subscribe({
      next: (data) => this.ressources = data,
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
}