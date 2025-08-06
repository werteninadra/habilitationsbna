import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';  // Import du FormsModule
import { BrowserModule } from '@angular/platform-browser';

import { ReactiveFormsModule } from '@angular/forms';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { TokenInterceptor } from './token.interceptor';
import { AppComponent } from './app.component'; // importe ton composant principal
import { HabilitationsComponent } from './habilitations/habilitations/habilitations.component';
import { AddUserComponent } from './components/add-user/add-user.component';
import { AppRoutingModule } from './app.routing.module'; // 👈 Assure-toi que ce chemin est correct
import { ModifierUserComponent } from './components/modifier-user/modifier-user.component';
import { ProfilComponent } from './components/profil/profil.component';
import { SignInComponent } from './sign-in/sign-in.component';
import { RouterModule } from '@angular/router';

@NgModule({
  declarations: [

  ],
  imports: [
    
    CommonModule,  // Ajout du CommonModule
    FormsModule,
     AppComponent,
      AppRoutingModule,
      SignInComponent,
     HabilitationsComponent,
     AddUserComponent, 
     ModifierUserComponent,
     ProfilComponent,
     // Ajout du composant AddUserComponent
    ReactiveFormsModule,  BrowserModule, // OBLIGATOIRE
 RouterModule,

    // Ajout du FormsModule pour utiliser ngModel
  ],


   providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: TokenInterceptor,
      multi: true
    }
    
  ]
  
})
export class ChambreModule {}
