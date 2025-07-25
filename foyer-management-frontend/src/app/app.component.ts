import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, RouterOutlet } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-root',
  standalone: true, // ✅ Important
  imports: [
    CommonModule,
    RouterOutlet,
    RouterModule, // ✅ Ne pas importer AppRoutingModule ici
    MatIconModule,FormsModule
  ],

  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'Habilitation';

  constructor(public router: Router) {}
}
