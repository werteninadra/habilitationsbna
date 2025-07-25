import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

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
    role: ''
  };

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.matricule = this.route.snapshot.params['matricule'];
    this.loadUserData();
  }

  loadUserData() {
    this.authService.getUserByMatricule(this.matricule).subscribe({
      next: (data) => {
        this.user = data;
      },
      error: (err) => {
        console.error('Erreur chargement utilisateur', err);
        this.router.navigate(['/habilitations']);
      }
    });
  }

  updateUser() {
    if (confirm('Êtes-vous sûr de vouloir modifier cet utilisateur ?')) {
      this.authService.updateUser(this.user.matricule, this.user).subscribe({
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