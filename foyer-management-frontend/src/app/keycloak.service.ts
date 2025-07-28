import { Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';

@Injectable({
  providedIn: 'root',
})
export class KeycloakService {
private keycloak!: Keycloak;

  init(): Promise<boolean> {
    return new Promise((resolve, reject) => {
      this.keycloak = new Keycloak({
        url: 'http://localhost:8080',
        realm: 'bna-realm',
        clientId: 'bna-client'
      });

      this.keycloak.init({
        onLoad: 'login-required',
        checkLoginIframe: false
      }).then(authenticated => {
        resolve(authenticated);
      }).catch(err => {
        reject(err);
      });
    });
  }

  getToken(): string | undefined {
    return this.keycloak?.token;
  }

  isLoggedIn(): boolean {
    return !!this.keycloak?.token;
  }

  getUsername(): string {
return this.keycloak?.tokenParsed?.['preferred_username'] || '';
  }

  logout(): void {
    this.keycloak.logout();
  }
}