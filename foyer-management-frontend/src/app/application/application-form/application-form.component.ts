import { Component, OnInit } from '@angular/core';
import { ApplicationService } from '../../services/application.service';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-application-form',
  templateUrl: './application-form.component.html',
  imports: [RouterModule, CommonModule, ReactiveFormsModule],
  styleUrls: ['./application-form.component.css']
})
export class ApplicationFormComponent implements OnInit {
  form: FormGroup;
  isEditMode = false;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private applicationService: ApplicationService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.form = this.fb.group({
      code: ['', [Validators.required, Validators.maxLength(20)]],
      libelle: ['', [Validators.required, Validators.maxLength(100)]],
      description: ['']
    });
  }

  ngOnInit(): void {
    const code = this.route.snapshot.paramMap.get('code');
    if (code) {
      this.isEditMode = true;
      this.loadApplication(code);
    }
  }

  loadApplication(code: string): void {
    this.isLoading = true;
    this.applicationService.getByCode(code).subscribe(
      app => {
        this.form.patchValue(app);
        this.isLoading = false;
      },
      error => {
        console.error('Error loading application', error);
        this.isLoading = false;
      }
    );
  }

  onSubmit(): void {
    if (this.form.valid) {
      this.isLoading = true;
      const appData = this.form.value;

      const operation = this.isEditMode
        ? this.applicationService.update(appData.code, appData)
        : this.applicationService.create(appData);

      operation.subscribe(
        () => {
          this.router.navigate(['/applications']);
        },
        error => {
          console.error('Error saving application', error);
          this.isLoading = false;
        }
      );
    }
  }
}