import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Agence, AgenceService, Occupation } from '../../services/agence.service';
import { CommonModule } from '@angular/common';
import { OccupationListComponent } from '../occupation-list/occupation-list.component';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

@Component({
  selector: 'app-agence-list',
  standalone: true,
  imports: [CommonModule, OccupationListComponent],
  templateUrl: './agence-list.component.html',
  styleUrls: ['./agence-list.component.css'],
})
export class AgenceListComponent implements OnInit {
  agences: Agence[] = [];
  error: string | null = null;

  selectedAgence?: Agence;
  occupations: Occupation[] = [];
  occupationError: string | null = null;

  private map?: any;  // Leaflet Map (any car import dynamique)

constructor(
  private agenceService: AgenceService,
  private router: Router,
  private sanitizer: DomSanitizer
) {}
  ngOnInit(): void {
    this.loadAgences();
  }

  loadAgences(): void {
    this.agenceService.getAgences().subscribe({
      next: (data) => {
        this.agences = data;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des agences';
        console.error(err);
      },
    });
  }

  deleteAgence(id?: number) {
    if (!id) return;
    if (confirm('Voulez-vous vraiment supprimer cette agence ?')) {
      this.agenceService.deleteAgence(id).subscribe({
        next: () => {
          this.loadAgences();
          if (this.selectedAgence?.id === id) {
            this.selectedAgence = undefined;
            this.occupations = [];
            this.destroyMap();
          }
        },
        error: (err) => alert('Erreur lors de la suppression'),
      });
    }
  }

  goToAdd() {
    this.router.navigate(['/agences/add']);
  }

  goToEdit(id?: number) {
    if (id) this.router.navigate(['/agences/edit', id]);
  }
getMapUrl(adresse?: string): SafeResourceUrl {
  if (!adresse) {
    return '';
  }
  const url = `https://www.google.com/maps?q=${encodeURIComponent(adresse)}&output=embed`;
  return this.sanitizer.bypassSecurityTrustResourceUrl(url);
}


  selectAgence(agence: Agence) {
    this.selectedAgence = agence;
    this.loadOccupations(agence.id!);

    if (this.map) {
      this.destroyMap();
    }

    if (agence.latitude !== undefined && agence.longitude !== undefined) {
      this.initMap(agence.latitude, agence.longitude, agence.nom);
    }
  }

  encodeURIComponent(str: string): string {
    return encodeURIComponent(str);
  }

  loadOccupations(agenceId: number) {
    this.agenceService.getOccupations(agenceId).subscribe({
      next: (data) => {
        this.occupations = data;
        this.occupationError = null;
      },
      error: (err) => {
        this.occupationError = 'Erreur lors du chargement des occupations';
        this.occupations = [];
        console.error(err);
      },
    });
  }

  private async initMap(lat: number, lng: number, nom: string) {
    // Import dynamique pour éviter erreur window undefined
    const L = await import('leaflet');

    this.map = L.map('map').setView([lat, lng], 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(this.map);

    L.marker([lat, lng])
      .addTo(this.map)
      .bindPopup(nom)
      .openPopup();
  }

  private destroyMap() {
    if (this.map) {
      this.map.remove();
      this.map = undefined;
    }
  }
}
