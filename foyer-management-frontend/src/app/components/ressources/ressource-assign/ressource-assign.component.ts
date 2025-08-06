import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { RessourceService } from '../../../services/ressource.service';
import { ProfilService } from '../../../services/ProfilService';
import { Profil } from '../../../models/profil';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-ressource-assign',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './ressource-assign.component.html',
  styleUrls: ['./ressource-assign.component.css']
})
export class RessourceAssignComponent implements OnInit {
  ressourceCode: string;
  selectedProfil: string = '';
  profils: Profil[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private ressourceService: RessourceService,
    private profilService: ProfilService
  ) {
    this.ressourceCode = this.route.snapshot.paramMap.get('code')!;
  }

  ngOnInit(): void {
    this.loadProfils();
  }

  loadProfils(): void {
    this.profilService.getAllProfils().subscribe({
      next: (profils) => this.profils = profils,
      error: (error) => console.error('Error loading profils', error)
    });
  }

  assignProfil(): void {
    if (this.selectedProfil) {
      this.ressourceService.assignToProfil(this.ressourceCode, this.selectedProfil).subscribe({
        next: () => {
          alert('Ressource assignée avec succès');
          this.router.navigate(['/ressources']);
        },
        error: (error) => console.error('Error assigning profil', error)
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/ressources']);
  }
}