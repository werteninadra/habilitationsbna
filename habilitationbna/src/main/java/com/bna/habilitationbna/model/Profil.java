package com.bna.habilitationbna.model;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "profils")
public class Profil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nom;

    private String description;

    @ElementCollection
    @CollectionTable(name = "profil_roles", joinColumns = @JoinColumn(name = "profil_id"))
    @Column(name = "role")
    private Set<String> roles;

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
}