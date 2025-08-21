package com.bna.habilitationbna.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
public class Profil {

    @Id
    @Column(nullable = false, unique = true)
    private String nom;

    @Column( nullable = false)
    private String description;
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(

            joinColumns = @JoinColumn(),
            inverseJoinColumns = @JoinColumn()
    )
    private Set<Ressource> ressources;

    // Getters/Setters


    public String getRole() { return this.nom; }
}
