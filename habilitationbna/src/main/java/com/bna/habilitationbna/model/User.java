package com.bna.habilitationbna.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

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
    @Column(unique = true, nullable = false)
    private String matricule;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String telephone;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_profils",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "profil_nom", referencedColumnName = "nom")
    )
    private Set<Profil> profils;

            public Set<String> getRoles() {
            return this.profils.stream()
            .map(Profil::getRole)
            .collect(Collectors.toSet());
            }
}