import { Ressource } from "./ressource";

export interface Profil {
  nom: string;
  description: string;
  ressources?: Ressource[];
}