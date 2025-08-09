import { Component, OnInit } from '@angular/core';
import { AgenceService, Agence, Occupation } from '../../services/agence.service';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-occupation-create',
  templateUrl: './occupation-create.component.html',
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  styleUrls: ['./occupation-create.component.css']
})
export class OccupationCreateComponent implements OnInit {
  agences: Agence[] = [];
  occupationForm!: FormGroup;
  successMessage: string = '';
  errorMessage: string = '';

  constructor(private agenceService: AgenceService, private fb: FormBuilder) {}

  ngOnInit(): void {
    this.agenceService.getAgences().subscribe(data => this.agences = data);

    this.occupationForm = this.fb.group({
      agenceId: ['', Validators.required],
      nombreClients: ['', [Validators.required, Validators.min(0)]],
      estFerie: [false],
      meteo: ['', Validators.required],
      jourSemaine: ['', [Validators.required, Validators.min(0), Validators.max(6)]]
    });
  }

  onSubmit() {
    if (this.occupationForm.valid) {
      const occupation: Occupation = this.occupationForm.value;
      this.agenceService.createOccupation(occupation).subscribe({
        next: res => {
          this.successMessage = 'Occupation créée avec succès !';
          this.errorMessage = '';
          this.occupationForm.reset();
        },
        error: err => {
          this.errorMessage = 'Erreur lors de la création de l\'occupation.';
          this.successMessage = '';
        }
      });
    }
  }
}
