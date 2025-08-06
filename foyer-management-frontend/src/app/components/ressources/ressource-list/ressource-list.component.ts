import { Component, OnInit, ViewChild, ElementRef, AfterViewInit, OnDestroy } from '@angular/core';
import { RessourceService } from '../../../services/ressource.service';
import { Ressource } from '../../../models/ressource';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from '../../../navbar/navbar.component'; // mets le bon chemin

import {
  Chart,
  ChartConfiguration,
  ChartType,
  BarController,
  BarElement,
  CategoryScale,
  LinearScale,
  Tooltip,
  Legend
} from 'chart.js';

// Enregistrer les éléments nécessaires pour le chart en barres
Chart.register(BarController, BarElement, CategoryScale, LinearScale, Tooltip, Legend);

@Component({
  selector: 'app-ressource-list',
  standalone: true,
  imports: [CommonModule, RouterModule, NavbarComponent],
  templateUrl: './ressource-list.component.html',
  styleUrls: ['./ressource-list.component.css']
})
export class RessourceListComponent implements OnInit, AfterViewInit, OnDestroy {
  ressources: Ressource[] = [];
  filteredRessources: Ressource[] = [];
  error: string | null = null;

  @ViewChild('appChart') appChartRef!: ElementRef<HTMLCanvasElement>;
  chart!: Chart;

  ressourcesLoaded = false;

  constructor(private ressourceService: RessourceService) { }

  ngOnInit(): void {
    this.loadRessources();
  }

  ngAfterViewInit(): void {
    if (this.ressourcesLoaded) {
      this.createChart();
    }
  }

  ngOnDestroy(): void {
    if (this.chart) {
      this.chart.destroy();
    }
  }

  loadRessources(): void {
    this.ressourceService.getAllRessourcesWithProfils().subscribe({
      next: (data) => {
        this.ressources = data;
        this.filteredRessources = data;
        this.ressourcesLoaded = true;
        if (this.appChartRef) {
          this.createChart();
        }
      },
      error: (err) => {
        console.error('Erreur complète:', err);
        this.error = 'Échec du chargement des ressources';
      }
    });
  }

 createChart(): void {
  if (!this.appChartRef) {
    console.warn('Canvas non disponible pour créer le chart');
    return;
  }

  // Calcul nombre ressources par application
  const appCount: { [key: string]: number } = {};
  this.ressources.forEach(res => {
    if (res.application) {
      const key = `${res.application.code} - ${res.application.libelle}`;
      appCount[key] = (appCount[key] || 0) + 1;
    }
  });

  const labels = Object.keys(appCount);
  const data = Object.values(appCount);

  if (this.chart) {
    this.chart.data.labels = labels;
    this.chart.data.datasets[0].data = data;
    this.chart.update();
    return;
  }

  const config: ChartConfiguration = {
    type: 'bar' as ChartType,
    data: {
      labels: labels,
      datasets: [{
        label: 'Nombre de ressources par application',
        data: data,
        backgroundColor: [
          'rgba(54, 162, 235, 0.6)',
          'rgba(255, 206, 86, 0.6)',
          'rgba(75, 192, 192, 0.6)',
          'rgba(153, 102, 255, 0.6)',
          'rgba(255, 99, 132, 0.6)'
        ],
        borderColor: [
          'rgba(54, 162, 235, 1)',
          'rgba(255, 206, 86, 1)',
          'rgba(75, 192, 192, 1)',
          'rgba(153, 102, 255, 1)',
          'rgba(255, 99, 132, 1)'
        ],
        borderWidth: 1
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      animation: {
        duration: 1000,
        easing: 'easeOutQuart'
      },
      plugins: {
        legend: {
          position: 'top',
        },
        tooltip: {
          enabled: true
        }
      },
      scales: {
        x: {
          title: {
            display: true,
            text: 'Applications'
          }
        },
        y: {
          beginAtZero: true,
          title: {
            display: true,
            text: 'Nombre de ressources'
          }
        }
      }
    }
  };

  this.chart = new Chart(this.appChartRef.nativeElement, config);
}

  deleteRessource(code: string): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette ressource ?')) {
      this.ressourceService.deleteRessource(code).subscribe(
        () => this.loadRessources(),
        error => console.error('Error deleting ressource', error)
      );
    }
  }

  onSearch(term: string): void {
    if (!term) {
      this.filteredRessources = this.ressources;
    } else {
      const lowerTerm = term.toLowerCase();
      this.filteredRessources = this.ressources.filter(r =>
        r.code.toLowerCase().includes(lowerTerm) ||
        r.libelle.toLowerCase().includes(lowerTerm) ||
        r.typeRessource.toLowerCase().includes(lowerTerm) ||
        (r.application && (r.application.code.toLowerCase().includes(lowerTerm) || r.application.libelle.toLowerCase().includes(lowerTerm)))
      );
    }
  }
}
