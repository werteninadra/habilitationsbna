import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ProfilService } from '../../services/ProfilService';

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
    profil: '' // Changé pour un seul profil
  };
  profils: any[] = [];

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private pservice: ProfilService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.matricule = this.route.snapshot.params['matricule'];
    this.loadUserData();
    this.loadProfils();
  }

 
  // modifier-user.component.ts

loadProfils() {
  this.pservice.getAllProfils().subscribe({
    next: (profils) => this.profils = profils,
    error: (err) => {
      console.error('Error loading profiles:', err);
      if (err.status === 401) {
        this.router.navigate(['/login']);
      }
    }
  });
}

  loadUserData() {
    this.authService.getUserByMatricule(this.matricule).subscribe({
      next: (data) => {
        this.user = data;
        // Prend le premier profil (puisqu'un seul maintenant)
        this.user.profil = data.profils[0]?.nom;
      },
      error: (err) => {
        console.error('Erreur chargement utilisateur', err);
        this.router.navigate(['/habilitations']);
      }
    });
  }

  updateUser() {
    if (confirm('Êtes-vous sûr de vouloir modifier cet utilisateur ?')) {
      const userToUpdate = {
        ...this.user,
        profils: [this.user.profil] // Envoie un tableau avec le seul profil sélectionné
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
  }

  goBack(): void {
    this.router.navigate(['/habilitations']);
  }
}