// app.routes.ts
import { Routes } from '@angular/router';
import { SignInComponent } from './sign-in/sign-in.component';
import { HabilitationsComponent } from './habilitations/habilitations/habilitations.component';
import { AddUserComponent } from './components/add-user/add-user.component';
import { ModifierUserComponent } from './components/modifier-user/modifier-user.component';

export const routes: Routes = [
  { path: '', redirectTo: 'habilitations', pathMatch: 'full' },
  // { path: 'sign-in', component: SignInComponent }, // Décommente si tu veux l'utiliser
  { path: 'habilitations', component: HabilitationsComponent },
  { path: 'register', component: AddUserComponent },
    //{ path: 'users/edit/:id', component: ModifierUserComponent },
 /*{path: 'users/edit/:id',
  loadComponent: () =>
    import('./components/modifier-user/modifier-user.component')
      .then(c => c.ModifierUserComponent)},*/
      { path: 'users/edit/:matricule', component: ModifierUserComponent },

      { path: 'modifier-utilisateur/:matricule', component:ModifierUserComponent},
{ path: 'modifier-user/:matricule', component: ModifierUserComponent }

];
