import { Component, Input, OnChanges } from '@angular/core';
import { Agence, AgenceService, Occupation } from '../../services/agence.service';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http'; // ⚠️ Ajouter HttpClient

@Component({
  selector: 'app-occupation-list',
  templateUrl: './occupation-list.component.html',
  imports: [CommonModule],  // Assure-toi d'importer les modules nécessaires ici si besoin
  styleUrls: ['./occupation-list.component.css']
})
export class OccupationListComponent implements OnChanges {
  @Input() agence!: Agence;
  
  occupations: Occupation[] = [];
  predictionToday: number | null = null;
  error: string | null = null;
  predictionMessage: string | null = null;

  constructor(private agenceService: AgenceService, private router: Router,private http: HttpClient) {}

  ngOnChanges(): void {
    this.loadOccupations();
    this.loadPrediction();
  }

  loadOccupations() {
    if (this.agence && this.agence.id !== undefined) {
      this.agenceService.getOccupations(this.agence.id).subscribe({
        next: (data) => {
          this.occupations = data;
          this.error = null;
        },
        error: (err) => {
          this.error = "Erreur lors du chargement des occupations";
          console.error(err);
        }
      });
    } else {
      this.error = "Agence ou ID agence non défini";
    }
  }

  loadPrediction() {
    if (this.agence && this.agence.id !== undefined) {
      this.agenceService.getPredictionToday(this.agence.id).subscribe({
        next: data => {
          console.log('Prediction reçue:', data);
          if (data && typeof data.message === 'string') {
            this.error = null;
            this.predictionToday = null;
            this.predictionMessage = data.message;
          } else {
            this.error = "Données de prédiction invalides";
            this.predictionMessage = null;
          }
        },
        error: err => {
          this.error = "Erreur lors du chargement de la prédiction";
          this.predictionMessage = null;
        }
      });
    } else {
      this.error = "Agence ou ID agence non défini";
    }
  }

  goToCreate() {
    this.router.navigate(['/occupations/create']);
  }

  editOccupation(occupation: Occupation) {
    if (occupation.id !== undefined) {
      this.router.navigate(['/occupations/edit', occupation.id]);
    }
  }



  deleteOccupation(id?: number) {
    if (!id) return;
    if (confirm('Voulez-vous vraiment supprimer cette occupation ?')) {
      this.agenceService.deleteOccupation(id).subscribe({
        next: () => {
          if (this.agence?.id !== undefined) {
            this.loadOccupations();  // Correction ici : loadOccupations sans paramètre
          }
        },
        error: (err) => alert('Erreur lors de la suppression de l\'occupation'),
      });
    }
  }
  
}
