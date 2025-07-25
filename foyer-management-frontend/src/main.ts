import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { importProvidersFrom } from '@angular/core';
import { AppRoutingModule } from './app/app.routing.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { KeycloakService } from './app/keycloak.service';

const keycloakService = new KeycloakService();

keycloakService.init().then(() => {
  bootstrapApplication(AppComponent, {
    providers: [
      provideHttpClient(withInterceptorsFromDi()),
      importProvidersFrom(
        AppRoutingModule,
        FormsModule,
        ReactiveFormsModule,
        BrowserAnimationsModule
      ),
      { provide: KeycloakService, useValue: keycloakService }
    ]
  });
}).catch(err => {
  console.error('Erreur d\'initialisation Keycloak', err);
});
