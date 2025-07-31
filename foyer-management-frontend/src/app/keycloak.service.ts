import { Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class KeycloakService {
  private keycloak!: Keycloak;
  private initPromise: Promise<boolean> | null = null;

  constructor(private router: Router) {}

  init(): Promise<boolean> {
  if (!this.initPromise) {
    this.initPromise = new Promise((resolve, reject) => {
      this.keycloak = new Keycloak({
        url: 'http://localhost:8080',
        realm: 'bna-realm',
        clientId: 'bna-client'
      });

      this.keycloak.init({
        onLoad: 'login-required',
        checkLoginIframe: false,
        pkceMethod: 'S256',
        redirectUri: window.location.origin + '/habilitations'
      }).then(authenticated => {
        if (authenticated) {
          localStorage.setItem('token', this.keycloak.token || '');
          localStorage.setItem('matricule', this.getUsername());
          this.router.navigate(['/habilitations']);
        }
        resolve(authenticated);
      }).catch(err => {
        console.error('Keycloak init failed', err);
        reject(err);
      });
    });
  }
  return this.initPromise;
}

  getToken(): string | undefined {
    return this.keycloak?.token;
  }

  isLoggedIn(): boolean {
    return !!this.keycloak?.authenticated;
  }

  getUsername(): string {
    return this.keycloak?.tokenParsed?.['preferred_username'] || '';
  }

  getMatricule(): string {
    return this.keycloak?.tokenParsed?.['matricule'] || this.getUsername();
  }

  logout(): Promise<void> {
    return new Promise((resolve, reject) => {
      const options = {
        redirectUri: window.location.origin + '/login'
      };
      
      this.keycloak?.logout(options)
        .then(() => {
          this.clearLocalData();
          this.router.navigate(['/login']);
          resolve();
        })
        .catch(err => {
          console.error('Logout failed', err);
          this.clearLocalData();
          this.router.navigate(['/login']);
          reject(err);
        });
    });
  }

  private clearLocalData(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('matricule');
  }
}