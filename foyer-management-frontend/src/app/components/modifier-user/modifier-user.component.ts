import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ProfilService } from '../../services/ProfilService';
import { AgenceService, Agence } from '../../services/agence.service';

@Component({
  selector: 'app-modifier-user',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './modifier-user.component.html',
  styleUrls: ['./modifier-user.component.css']
})

export class ModifierUserComponent implements OnInit {
  matricule!: string;
  user: any = {
    matricule: '',
    nom: '',
    prenom: '',
    telephone: '',
    email: '',
    profil: '',
    agenceId: null // <-- ajoute ce champ
  };
  profils: any[] = [];
  agences: Agence[] = []; // <-- liste des agences

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private pservice: ProfilService,
    private agenceService: AgenceService, // <-- injecter le service Agence
    private router: Router
  ) {}

  ngOnInit(): void {
    this.matricule = this.route.snapshot.params['matricule'];
    this.loadProfils();
    this.loadAgences(); // <-- charger les agences
    this.loadUserData();
  }

  loadProfils() {
    this.pservice.getAllProfils().subscribe({
      next: (profils) => this.profils = profils,
      error: (err) => {
        console.error('Erreur chargement profils:', err);
        if (err.status === 401) this.router.navigate(['/login']);
      }
    });
  }

  loadAgences() {
    this.agenceService.getAgences().subscribe({
      next: (agences) => this.agences = agences,
      error: (err) => console.error('Erreur chargement agences:', err)
    });
  }

  loadUserData() {
    this.authService.getUserByMatricule(this.matricule).subscribe({
      next: (data) => {
        this.user = data;
        this.user.profil = data.profils[0]?.nom;
        this.user.agenceId = data.agence?.id; // <-- pré-remplir l'agence
      },
      error: (err) => {
        console.error('Erreur chargement utilisateur', err);
        this.router.navigate(['/habilitations']);
      }
    });
  }

  updateUser() {
    if (!confirm('Êtes-vous sûr de vouloir modifier cet utilisateur ?')) return;

    const userToUpdate = {
      ...this.user,
      profils: [this.user.profil]
    };

    this.authService.updateUser(this.user.matricule, userToUpdate).subscribe({
      next: () => {
        alert('Utilisateur mis à jour avec succès');
        this.router.navigate(['/habilitations']);
      },
      error: (err) => {
        console.error('Erreur mise à jour', err);
        alert('Erreur lors de la mise à jour');
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/habilitations']);
  }
}
