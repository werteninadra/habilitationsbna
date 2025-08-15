import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Agence, AgenceService, Occupation } from '../../services/agence.service';
import { CommonModule } from '@angular/common';
import { OccupationListComponent } from '../occupation-list/occupation-list.component';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { NavbarComponent } from '../../navbar/navbar.component'; // Chemin à adapter
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-agence-list',
  standalone: true,
  imports: [CommonModule, OccupationListComponent, NavbarComponent],
  templateUrl: './agence-list.component.html',
  styleUrls: ['./agence-list.component.css'],
})
export class AgenceListComponent implements OnInit {
  agences: Agence[] = [];
  filteredAgences: Agence[] = [];
  error: string | null = null;

  selectedAgence?: Agence;
  occupations: Occupation[] = [];
  occupationError: string | null = null;

  private map?: any;

  constructor(
    private agenceService: AgenceService,
    private router: Router,
    private sanitizer: DomSanitizer,
     public authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.loadAgences();
  }

  loadAgences(): void {
    this.agenceService.getAgences().subscribe({
      next: (data) => {
        this.agences = data;
        this.filteredAgences = data; // Initialisation pour recherche
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
        error: () => alert('Erreur lors de la suppression'),
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
    if (!adresse) return '';
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

  loadOccupations(agenceId: number) {
    this.agenceService.getOccupations(agenceId).subscribe({
      next: (data) => {
        this.occupations = data;
        this.occupationError = null;
      },
      error: () => {
        this.occupationError = 'Erreur lors du chargement des occupations';
        this.occupations = [];
      },
    });
  }

  onSearch(term: string) {
    if (!term) {
      this.filteredAgences = this.agences;
    } else {
      const lower = term.toLowerCase();
      this.filteredAgences = this.agences.filter(a =>
        a.nom.toLowerCase().includes(lower) ||
        (a.adresse && a.adresse.toLowerCase().includes(lower))
      );
    }
  }

  private async initMap(lat: number, lng: number, nom: string) {
    const L = await import('leaflet');
    this.map = L.map('map').setView([lat, lng], 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(this.map);

    L.marker([lat, lng]).addTo(this.map).bindPopup(nom).openPopup();
  }

  private destroyMap() {
    if (this.map) {
      this.map.remove();
      this.map = undefined;
    }
  }
}
