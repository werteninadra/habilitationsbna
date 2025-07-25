import { Component, EventEmitter, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { NgIf } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-add-user',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf],
  templateUrl: './add-user.component.html',
  styleUrls: ['./add-user.component.css']
})
export class AddUserComponent {
  userForm: FormGroup;
  @Output() userAdded = new EventEmitter<void>();

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {
    this.userForm = this.fb.group({
      matricule: ['', [Validators.required, Validators.pattern(/^03\d{18}$/)]],
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      telephone: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
      role: ['', Validators.required]
    });
  }

  onSubmit() {
    if (this.userForm.invalid) return;

    this.authService.register(this.userForm.value).subscribe({
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