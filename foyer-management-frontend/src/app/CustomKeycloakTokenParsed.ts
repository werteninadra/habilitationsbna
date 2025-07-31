import { KeycloakTokenParsed } from 'keycloak-js';

export interface CustomKeycloakTokenParsed extends KeycloakTokenParsed {
  preferred_username?: string;
  email?: string;
  matricule?: string;
  given_name?: string;
  family_name?: string;
  // Ajoutez d'autres claims personnalisés ici
}