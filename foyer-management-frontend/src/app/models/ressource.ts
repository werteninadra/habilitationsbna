import { Profil } from "./profil";
import { Application } from "./Application";
export interface Ressource {
  code: string;
  libelle: string;
  typeRessource: string;
  statut: boolean;
  application?: Application;
  profils?: Profil[];
}