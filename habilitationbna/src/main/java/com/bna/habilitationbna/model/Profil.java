package com.bna.habilitationbna.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.Collections;
import java.util.Set;

@Entity
@Table(name = "profils")
public class Profil {

    @Id
    @Column(name = "nom", nullable = false, unique = true)
    private String nom;

    @Column(name = "LIB_PFL_PFL", nullable = false)
    private String description;
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "profil_ressource",
            joinColumns = @JoinColumn(name = "nom", referencedColumnName = "nom"),
            inverseJoinColumns = @JoinColumn(name = "COD_RES_RES", referencedColumnName = "COD_RES_RES")
    )
    private Set<Ressource> ressources;

    // Getters/Setters
    public String getNom() { return nom; }

    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public Set<Ressource> getRessources() { return ressources; }

    public void setRessources(Set<Ressource> ressources) { this.ressources = ressources; }

    public String getRole() { return this.nom; }
}
