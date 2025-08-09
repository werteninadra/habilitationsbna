import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Agence, AgenceService } from '../../services/agence.service';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-agence-form',
    imports: [RouterModule, CommonModule, ReactiveFormsModule, FormsModule],
  styleUrls: ['./agence-form.component.css'],

  templateUrl: './agence-form.component.html',
})
export class AgenceFormComponent implements OnInit {
  
  agence: Agence = { nom: '', capaciteMax: 0 };
  isEditMode = false;
  error: string | null = null;

  constructor(
    private agenceService: AgenceService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.agenceService.getAgence(+id).subscribe({
        next: (data) => (this.agence = data),
        error: (err) => (this.error = 'Erreur lors du chargement de l\'agence'),
      });
    }
  }
save() {
  if (!this.agence.nom || this.agence.capaciteMax <= 0) {
    alert('Veuillez remplir tous les champs correctement');
    return;
  }
  if (this.isEditMode && this.agence.id) {
    this.agenceService.updateAgence(this.agence).subscribe({
      next: () => this.router.navigate(['/agences']),
      error: (err) => (this.error = 'Erreur lors de la mise à jour'),
    });
  } else {
    this.agenceService.createAgence(this.agence).subscribe({
      next: () => this.router.navigate(['/agences']),
      error: (err) => (this.error = 'Erreur lors de la création'),
    });
  }
}


  cancel() {
    this.router.navigate(['/agences']);
  }
}
