import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AgenceService, Occupation } from '../../services/agence.service'; // ✅ Import correct
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-edit-occupation',
  imports: [ CommonModule , FormsModule], // Assure-toi d'importer les modules nécessaires ici si besoin,
  templateUrl: './edit-occupation.component.html'
})
export class EditOccupationComponent implements OnInit {
  occupation: Occupation = {
    nombreClients: 0,
    estFerie: false,
    meteo: '',
    jourSemaine: 1,
    agenceId: 0
  };

  constructor(
    private route: ActivatedRoute,
    private agenceService: AgenceService, // ✅ Service correct
    private router: Router
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.agenceService.getOccupationById(id).subscribe(data => { // ✅ méthode à ajouter
        this.occupation = data;
      });
    }
  }

  updateOccupation() {
    this.agenceService.updateOccupation(this.occupation).subscribe(() => {
      this.router.navigate(['/occupations']);
    });
  }
  // Ajoute une méthode pour gérer la suppression si nécessaire
}
