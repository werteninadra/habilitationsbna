package com.bna.habilitationbna.model;

import jakarta.persistence.*;
import java.util.Collections;
import java.util.Set;

@Entity
@Table(name = "profils")
public class Profil {

    @Id
    @Column(nullable = false, unique = true)
    private String nom; // Ce nom sert aussi de rôle

    @Column(nullable = false)
    private String description;

    // Supprimer les rôles supplémentaires
    // @ElementCollection et autres annotations relatives aux rôles suppl.

    // Getters et Setters
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Remplacer getAllRoles() par getRole() (puisqu'un profil = un rôle)
    public String getRole() {
        return this.nom;
    }
}