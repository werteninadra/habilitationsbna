import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Chart, ChartConfiguration, ChartType } from 'chart.js';

@Component({
  selector: 'app-statistiques',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './statistiques.component.html',
  styleUrls: ['./statistiques.component.css']
})
export class StatistiquesComponent implements OnInit, OnDestroy {
  @ViewChild('myChart', { static: true }) myChartRef!: ElementRef<HTMLCanvasElement>;
  chart!: Chart;

  ngOnInit(): void {
    this.initChart();
  }

  ngOnDestroy(): void {
    if (this.chart) {
      this.chart.destroy();
    }
  }

  initChart(): void {
    const config: ChartConfiguration = {
      type: 'line' as ChartType,
      data: {
        labels: ['Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi'],
        datasets: [{
          label: 'Utilisateurs actifs',
          data: [15, 30, 20, 40, 25],
          fill: true,
          borderColor: 'green',
          backgroundColor: 'rgba(0, 255, 0, 0.2)',
          tension: 0.5,
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false
      }
    };

    this.chart = new Chart(this.myChartRef.nativeElement, config);
  }
}
