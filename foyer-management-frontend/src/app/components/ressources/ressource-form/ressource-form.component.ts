import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { RessourceService } from '../../../services/ressource.service';
import { ApplicationService } from '../../../services/application.service';
import { Ressource } from '../../../models/ressource';
import { Application } from '../../../models/Application';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-ressource-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './ressource-form.component.html',
  styleUrls: ['./ressource-form.component.css']
})
export class RessourceFormComponent implements OnInit {
  ressourceForm: FormGroup;
  isEditMode = false;
  ressourceCode: string | null = null;
  applications: Application[] = [];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private ressourceService: RessourceService,
    private applicationService: ApplicationService
  ) {
    this.ressourceForm = this.fb.group({
      code: ['', Validators.required],
      libelle: ['', Validators.required],
      typeRessource: ['', Validators.required],
      statut: [true],
        tempsEstimeJours: [{value: '', disabled: true}] , // ajout lecture seule

      applicationCode: [null]
    });
  }

  ngOnInit(): void {
    this.loadApplications();
    
    this.ressourceCode = this.route.snapshot.paramMap.get('code');
    this.isEditMode = !!this.ressourceCode;

    if (this.isEditMode) {
      this.ressourceService.getRessourceById(this.ressourceCode!).subscribe(
        ressource => {
          this.ressourceForm.patchValue({
            code: ressource.code,
            libelle: ressource.libelle,
            typeRessource: ressource.typeRessource,
            statut: ressource.statut,
            
  tempsEstimeJours: ressource.tempsEstimeJours ?? null,
            applicationCode: ressource.application?.code || null
          });
        },
        error => console.error('Error loading ressource', error)
      );
    }
  }

  loadApplications(): void {
    this.applicationService.getAll().subscribe({
      next: (apps) => this.applications = apps,
      error: (err) => console.error('Error loading applications', err)
    });
  }

  onSubmit(): void {
    if (this.ressourceForm.valid) {
      const formData = this.ressourceForm.value;
      const selectedApp = this.applications.find(app => app.code === formData.applicationCode);
      
      const ressourceData: Ressource = {
        code: formData.code,
        libelle: formData.libelle,
        typeRessource: formData.typeRessource,
        statut: formData.statut,
        application: selectedApp
      };

      if (this.isEditMode) {
        this.ressourceService.updateRessource(this.ressourceCode!, ressourceData).subscribe({
          next: () => this.router.navigate(['/ressources']),
          error: (error) => console.error('Error updating ressource', error)
        });
      } else {
        this.ressourceService.createRessource(ressourceData).subscribe({
          next: () => this.router.navigate(['/ressources']),
          error: (error) => console.error('Error creating ressource', error)
        });
      }
    }
  }
  
  cancel(): void {
    this.router.navigate(['/ressources']);
  }
}