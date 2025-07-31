package com.bna.habilitationbna.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Pattern(regexp = "^03\\d{18}$", message = "Le matricule doit commencer par 03 et contenir exactement 20 chiffres")
    @Column(unique = true, nullable = false, length = 20)
    private String matricule;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String telephone;

    @Column(name = "is_active", nullable = false)
    @ColumnDefault("true")
    private Boolean active = true;

    @Column(name = "is_blocked", nullable = false)
    @ColumnDefault("false")
    private Boolean blocked = false;

    @Column(name = "profile_image_path")
    private String profileImagePath;

    @Column(name = "last_login")
    private Instant lastLogin;

    @Column(name = "is_logged_in", nullable = false)
    @ColumnDefault("false")
    private Boolean loggedIn = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_profils",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "profil_nom", referencedColumnName = "nom")
    )
    private Set<Profil> profils = new HashSet<>();

    // Business methods
    public void activate() {
        this.active = true;
        this.blocked = false;
        this.lastLogin = Instant.now();
    }

    public void logout() {
        this.loggedIn = false;
        this.lastLogin = Instant.now();
    }

    public void deactivate() {
        this.active = false;
    }

    public Set<String> getRoles() {
        return this.profils.stream()
                .map(Profil::getRole)
                .collect(Collectors.toSet());
    }

    // Ensure default values are set before persisting
    @PrePersist
    public void setDefaultValues() {
        if (this.active == null) {
            this.active = true;
        }
        if (this.blocked == null) {
            this.blocked = false;
        }
        if (this.loggedIn == null) {
            this.loggedIn = false;
        }
    }
}