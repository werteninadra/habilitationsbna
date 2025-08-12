import { Profil } from "./profil";
import { Application } from "./Application";
export interface Ressource {
  code: string;
  libelle: string;
  typeRessource: string;
  statut: boolean;
    tempsEstimeJours?: number;  // <- Ajoute ce champ optionnel

  application?: Application;
  profils?: Profil[];
}