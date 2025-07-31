import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { provideRouter } from '@angular/router';
import { routes } from './app/app.routes';
import { KeycloakService } from './app/keycloak.service';
import { provideHttpClient } from '@angular/common/http';
import { APP_INITIALIZER } from '@angular/core'; // Ajoutez cette importation

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideHttpClient(),
    KeycloakService,
    {
      provide: APP_INITIALIZER,
      useFactory: (keycloak: KeycloakService) => () => keycloak.init(),
      multi: true,
      deps: [KeycloakService]
    }
  ]
}).catch(err => console.error(err));