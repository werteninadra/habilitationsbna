package com.bna.habilitationbna.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
public class Ressource {
        @Id
        @Column( nullable = false, unique = true)
        private String code;
        private String ipfsUrl;
        @Column(nullable = false)
        private String libelle;

        @Column
        private String typeRessource;
        @Column
        private Double tempsEstimeJours;


        @Column()
        private boolean statut;
        @ManyToOne
        @JoinColumn()
        private Application application;

        @JsonIgnoreProperties("ressources") // Solution alternative

// relation inverse (facultative si bidirectionnelle)
        @ManyToMany(mappedBy = "ressources")
        private Set<Profil> profils;
}
