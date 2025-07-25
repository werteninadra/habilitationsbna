import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-habilitations',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './habilitations.component.html',
  styleUrls: ['./habilitations.component.css']
})
export class HabilitationsComponent implements OnInit {
  users: any[] = [];
  filteredUsers: any[] = [];
  showSection = 'user-management';
  searchTerm: string = '';
  
  // Tri
  sortField: string = 'matricule';
  sortDirection: 'asc' | 'desc' = 'asc';
  
  // Pagination
  currentPage: number = 1;
  itemsPerPage: number = 5;
  totalItems: number = 0;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.authService.getAllUsersWithDetails().subscribe({
      next: (data) => {
        this.users = data;
        this.filteredUsers = [...this.users];
        this.totalItems = this.filteredUsers.length;
        console.log('Users loaded:', this.users.length);
        console.log('Total pages:', this.totalPages);
        this.sortUsers(this.sortField);
      },
      error: (err) => {
        console.error('Erreur lors du chargement des utilisateurs', err);
      }
    });
  }

  searchUsers(): void {
    if (!this.searchTerm) {
      this.filteredUsers = [...this.users];
    } else {
      const term = this.searchTerm.toLowerCase();
      this.filteredUsers = this.users.filter(user => 
        (user.matricule?.toLowerCase().includes(term) || '') ||
        (user.nom?.toLowerCase().includes(term) || '') ||
        (user.prenom?.toLowerCase().includes(term) || '') ||
        (user.email?.toLowerCase().includes(term) || '') ||
        (user.telephone?.toLowerCase().includes(term) || '')
      );
    }
    this.totalItems = this.filteredUsers.length;
    this.currentPage = 1;
    this.sortUsers(this.sortField);
  }

  sortUsers(field: string): void {
    if (this.sortField === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = 'asc';
    }

    this.filteredUsers.sort((a, b) => {
      const valA = a[field]?.toString().toLowerCase() || '';
      const valB = b[field]?.toString().toLowerCase() || '';
      
      if (valA < valB) return this.sortDirection === 'asc' ? -1 : 1;
      if (valA > valB) return this.sortDirection === 'asc' ? 1 : -1;
      return 0;
    });
  }

  // Pagination
  get paginatedUsers(): any[] {
    const start = (this.currentPage - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    return this.filteredUsers.slice(start, end);
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.totalItems / this.itemsPerPage));
  }

  changePage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }

  getPageNumbers(): number[] {
    const pagesToShow = 5;
    let startPage = Math.max(1, this.currentPage - Math.floor(pagesToShow / 2));
    let endPage = startPage + pagesToShow - 1;

    if (endPage > this.totalPages) {
      endPage = this.totalPages;
      startPage = Math.max(1, endPage - pagesToShow + 1);
    }

    return Array.from({length: endPage - startPage + 1}, (_, i) => startPage + i);
  }

  onItemsPerPageChange(): void {
    this.currentPage = 1;
  }

  // Navigation
  navigateToRegister(): void {
    this.router.navigate(['/register']);
  }

  goToEditUser(matricule: string): void {
    if (matricule) {
      this.router.navigate(['/modifier-user', matricule]);
    }
  }

  deleteUser(matricule: string): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cet utilisateur ?')) {
      this.authService.deleteUser(matricule).subscribe({
        next: () => {
          alert('Utilisateur supprimé avec succès');
          this.loadUsers();
        },
        error: (err) => {
          console.error('Erreur lors de la suppression', err);
          alert('Échec de la suppression: ' + (err.error?.message || err.message));
        }
      });
    }
  }
}