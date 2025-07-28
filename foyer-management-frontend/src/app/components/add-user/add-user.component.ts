import { Component, EventEmitter, Output, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { CommonModule, NgIf } from '@angular/common';
import { Router } from '@angular/router';
import { ProfilService } from '../../services/ProfilService';

@Component({
  selector: 'app-add-user',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf,CommonModule],
  templateUrl: './add-user.component.html',
  styleUrls: ['./add-user.component.css']
})
export class AddUserComponent implements OnInit {
  userForm: FormGroup;
  profils: any[] = [];
  @Output() userAdded = new EventEmitter<void>();

  constructor(
    private fb: FormBuilder, 
    private authService: AuthService, 
        private pservice: ProfilService,
    
    private router: Router
  ) {
    this.userForm = this.fb.group({
      matricule: ['', [Validators.required, Validators.pattern(/^03\d{18}$/)]],
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      telephone: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
      profil: ['', Validators.required] // Changé pour un seul profil
    });
  }

  ngOnInit(): void {
    this.loadProfils();
  }
// add-user.component.ts

loadProfils() {
  this.pservice.getAllProfils().subscribe({
    next: (profils) => {
      this.profils = profils;
    },
    error: (err) => {
      console.error('Error loading profiles:', err);
      // Add user feedback here (toast, alert, etc.)
      if (err.status === 401) {
        this.router.navigate(['/login']); // Redirect if unauthorized
      }
    }
  });
}
  

  onSubmit() {
    if (this.userForm.invalid) return;

    const formData = {
      ...this.userForm.value,
      profils: [this.userForm.value.profil] // Envoie un tableau avec le seul profil sélectionné
    };

    this.authService.register(formData).subscribe({
      next: (response) => {
        this.userForm.reset();
        this.userAdded.emit();
        this.router.navigate(['/habilitations']);
      },
      error: err => {
        console.error('Erreur lors de l\'ajout:', err);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/habilitations']);
  }
}