import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProfilService } from '../../services/ProfilService';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-profil',
    imports: [CommonModule, FormsModule,ReactiveFormsModule],

  templateUrl: './profil.component.html',
  styleUrls: ['./profil.component.css']
})
export class ProfilComponent implements OnInit {
  profilForm: FormGroup;
  profils: any[] = [];
  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  constructor(
    private fb: FormBuilder,
    private profilService: ProfilService
  ) {
    this.profilForm = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.maxLength(255)]]
    });
  }

  ngOnInit(): void {
    this.loadProfils();
  }

  get nom() {
    return this.profilForm.get('nom');
  }

  get description() {
    return this.profilForm.get('description');
  }

  loadProfils(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.profils = [];

    this.profilService.getAllProfils().subscribe({
      next: (profils) => {
        this.profils = profils;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.status === 401 
          ? 'Accès non autorisé. Veuillez vous reconnecter.' 
          : 'Erreur lors du chargement des profils';
        this.isLoading = false;
      }
    });
  }

  addProfil(): void {
    if (this.profilForm.invalid) return;

    this.isLoading = true;
    this.errorMessage = null;
    this.successMessage = null;

    this.profilService.createProfil(this.profilForm.value).subscribe({
      next: () => {
        this.successMessage = 'Profil créé avec succès';
        this.profilForm.reset();
        this.loadProfils();
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.status === 401 
          ? 'Non autorisé. Veuillez vous reconnecter.'
          : (err.error?.message || 'Erreur lors de la création du profil');
        this.isLoading = false;
      }
    });

    console.log('Formulaire envoyé:', this.profilForm.value);
  }
}
