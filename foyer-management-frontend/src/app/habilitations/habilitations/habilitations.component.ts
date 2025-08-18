import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { KeycloakService } from '../../keycloak.service';

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
  currentUser: any = {
    nom: '',
    prenom: '',
    profileImagePath: '',
    matricule: '',
    active: false,
    blocked: false
  };
  
  // Tr
  sortField: string = 'matricule';
  sortDirection: 'asc' | 'desc' = 'asc';
  
  // Pagination
  currentPage: number = 1;
  itemsPerPage: number = 5;
  totalItems: number = 0;

  constructor(
    public authService: AuthService,
    private router: Router,
        private keycloakService: KeycloakService
    
  ) {}
  //
  ngOnInit(): void {
    this.loadCurrentUser();
    this.loadUsers();
  }

  loadCurrentUser(): void {
  this.authService.getCurrentUser().subscribe({
    next: (user) => {
      this.currentUser = {
        nom: user?.nom || 'Invité',
        prenom: user?.prenom || '',
        profileImagePath: user?.profileImagePath || '',
        matricule: user?.matricule || '',
        active: user?.active || false,
        blocked: user?.blocked || false
      };
      
      // Correction du chemin de l'image si nécessaire
      if (this.currentUser.profileImagePath && !this.currentUser.profileImagePath.startsWith('http')) {
        this.currentUser.profileImagePath = `http://localhost:8081${this.currentUser.profileImagePath}`;
      }
    },
    error: (err) => {
      console.error('Erreur chargement utilisateur', err);
      this.resetCurrentUser();
      this.router.navigate(['/login']);
    }
  });
}
  public getProfileImageUrl(path: string | undefined | null): string {
    if (!path) return '';
    return path.startsWith('http') ? path : `http://localhost:8081${path}`;
  }

  private resetCurrentUser(): void {
    this.currentUser = {
      nom: 'Invité',
      prenom: '',
      profileImagePath: '',
      matricule: '',
      active: false,
      blocked: false
    };
  }

  loadUsers(): void {
    this.authService.getAllUsersWithDetails().subscribe({
      next: (data) => {
        this.users = data;
        this.filteredUsers = [...this.users];
        this.totalItems = this.filteredUsers.length;
        this.sortUsers(this.sortField);
      },
      error: (err) => {
        console.error('Erreur lors du chargement des utilisateurs', err);
        if (err.status === 401) {
          this.router.navigate(['/login']);
        }
      }
    });
  }
// In habilitations.component.ts
logout() {
  this.authService.logout().subscribe({
    next: () => {
      // Successfully logged out from both backend and Keycloak
      this.router.navigate(['/login']);
    },
    error: (err) => {
      console.error('Logout error:', err);
      // Fallback to Keycloak logout if backend logout fails
      this.keycloakService.logout().then(() => {
        this.router.navigate(['/login']);
      });
    }
  });
}

private navigateToLogin() {
  // Clear any remaining auth data
  this.authService.clearLocalData();
  
  // Use window.location as fallback if router fails
  try {
    this.router.navigate(['/login']).catch(() => {
      window.location.href = '/login';
    });
  } catch {
    window.location.href = '/login';
  }
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

  // Paginati
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
    this.loadUsers();
  }

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
          if (err.status === 401) {
            this.router.navigate(['/login']);
          }
        }
      });
    }
  }

  toggleUserStatus(matricule: string, currentStatus: boolean): void {
    const action = currentStatus ? 'block' : 'unblock';
    const confirmMessage = currentStatus 
      ? 'Êtes-vous sûr de vouloir bloquer cet utilisateur ?' 
      : 'Êtes-vous sûr de vouloir débloquer cet utilisateur ?';

    if (confirm(confirmMessage)) {
      this.authService.toggleUserStatus(matricule, action).subscribe({
        next: () => {
          alert(`Utilisateur ${action === 'block' ? 'bloqué' : 'débloqué'} avec succès`);
          this.loadUsers();
        },
        error: (err) => {
          console.error('Erreur changement statut', err);
          alert('Échec de l\'opération: ' + err.error?.message);
          if (err.status === 401) {
            this.router.navigate(['/login']);
          }
        }
      });
    }
  }
onFileSelected(event: any, matricule: string): void {
  const file: File = event.target.files[0];
  if (file) {
    const formData = new FormData();
    formData.append('file', file);

    this.authService.uploadProfileImage(matricule, formData).subscribe({
      next: (response: any) => {
        // Mise à jour de l'utilisateur courant si c'est son image
        if (matricule === this.currentUser.matricule) {
          this.currentUser.profileImagePath = response.imagePath;
          if (!this.currentUser.profileImagePath.startsWith('http')) {
            this.currentUser.profileImagePath = `http://localhost:8081${response.imagePath}`;
          }
        }
        this.loadUsers(); // Recharger la liste
      },
      error: (err) => {
        console.error('Erreur upload image', err);
        alert('Échec du téléchargement: ' + err.error?.message);
      }
    });
  }
}

  getUserStatusBadgeClass(user: any): string {
    if (user.blocked) return 'badge-danger';
    if (!user.active) return 'badge-warning';
    return 'badge-success';
  }

  getUserStatusText(user: any): string {
    if (user.blocked) return 'Bloqué';
    if (!user.active) return 'Inactif';
    return 'Actif';
  }




  //jjfzjj
}