// app.routes.ts
import { Routes } from '@angular/router';
import { SignInComponent } from './sign-in/sign-in.component';
import { HabilitationsComponent } from './habilitations/habilitations/habilitations.component';
import { AddUserComponent } from './components/add-user/add-user.component';
import { ModifierUserComponent } from './components/modifier-user/modifier-user.component';
import { ProfilComponent } from './components/profil/profil.component';
import { RessourceAssignComponent } from './components/ressources/ressource-assign/ressource-assign.component';
import { RessourceFormComponent } from './components/ressources/ressource-form/ressource-form.component';
import { RessourceListComponent } from './components/ressources/ressource-list/ressource-list.component';
import { ApplicationFormComponent } from './application/application-form/application-form.component';
import { ApplicationListComponent } from './application/application-list/application-list.component';
import { OccupationListComponent } from './components/occupation-list/occupation-list.component';
import { AgenceListComponent } from './components/agence-list/agence-list.component';
import { OccupationCreateComponent } from './components/occupation-create/occupation-create.component';
import { AgenceFormComponent } from './components/agence-form/agence-form.component';
import { EditOccupationComponent } from './occupations/edit-occupation/edit-occupation.component';

export const routes: Routes = [
  { path: '', redirectTo: 'habilitations', pathMatch: 'full' },
  { path: 'habilitations', component: HabilitationsComponent },
  { path: 'register', component: AddUserComponent },
    //{ path: 'users/edit/:id', component: ModifierUserComponent },
 /*{path: 'users/edit/:id',
  loadComponent: () =>
    import('./components/modifier-user/modifier-user.component')
      .then(c => c.ModifierUserComponent)},*/
      { path: 'users/edit/:matricule', component: ModifierUserComponent },
{ path: 'gestion-profils', component: ProfilComponent  },
      { path: 'modifier-utilisateur/:matricule', component:ModifierUserComponent},
{ path: 'modifier-user/:matricule', component: ModifierUserComponent },
{ path: 'ressources', component: RessourceListComponent },
  { path: 'ressources/new', component: RessourceFormComponent },
  { path: 'ressources/:code/edit', component: RessourceFormComponent },
  { path: 'ressources/:code/assign', component: RessourceAssignComponent },
   { path: 'applications', component: ApplicationListComponent },
  { path: 'applications/new', component: ApplicationFormComponent },
  {
    path: 'occupations/create',
    component: OccupationCreateComponent,
  },
  { path: 'applications/:code/edit', component: ApplicationFormComponent },
  { path: 'agences', component: AgenceListComponent },
  { path: 'occupations/:agenceId', component: OccupationListComponent },
 { path: 'agences/add', component: AgenceFormComponent },
   {path: 'agences/edit/:id', component: AgenceFormComponent },
   { path: 'occupations/edit/:id', component: EditOccupationComponent }
];
