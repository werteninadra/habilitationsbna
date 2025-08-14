import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  imports: [RouterModule, CommonModule,FormsModule ],
  styleUrls: ['./navbar.component.css'],
  standalone: true
})
export class NavbarComponent {
  searchTerm: string = '';
  @Output() search = new EventEmitter<string>();

  onSearchChange() {
    this.search.emit(this.searchTerm.trim());
  }


  ///deklaration of the function to handle search
}
