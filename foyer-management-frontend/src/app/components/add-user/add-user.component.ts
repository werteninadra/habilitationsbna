import { Component, EventEmitter, Output, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { ProfilService } from '../../services/ProfilService';
import { AgenceService, Agence } from '../../services/agence.service';
import { CommonModule, NgIf } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-add-user',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf, CommonModule],
  templateUrl: './add-user.component.html',
  styleUrls: ['./add-user.component.css']
})
export class AddUserComponent implements OnInit {
  userForm: FormGroup;
  profils: any[] = [];
  agences: Agence[] = [];
  @Output() userAdded = new EventEmitter<void>();

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private pservice: ProfilService,
    private agenceService: AgenceService,
    private router: Router
  ) {
    this.userForm = this.fb.group({
      matricule: ['', [Validators.required]],
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      telephone: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
      profil: ['', Validators.required],
      agenceId: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadProfils();
    this.loadAgences();
  }

  loadProfils() {
    this.pservice.getAllProfils().subscribe({
      next: profils => this.profils = profils,
      error: err => console.error('Erreur loading profils', err)
    });
  }

  loadAgences() {
    this.agenceService.getAgences().subscribe({
      next: agences => this.agences = agences,
      error: err => console.error('Erreur loading agences', err)
    });
  }

  onSubmit() {
    if (this.userForm.invalid) return;

    const formData = {
      ...this.userForm.value,
      profils: [this.userForm.value.profil], // tableau pour le backend
      agenceId: this.userForm.value.agenceId // envoie ID
    };

    this.authService.register(formData).subscribe({
      next: () => {
        this.userForm.reset();
        this.userAdded.emit();
        this.router.navigate(['/habilitations']);
      },
      error: err => console.error('Erreur lors de l\'ajout:', err)
    });
  }

  goBack(): void {
    this.router.navigate(['/habilitations']);
  }
}
