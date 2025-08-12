package com.bna.habilitationbna.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "ressources")
@Getter
@Setter
public class Ressource {
        @Id
        @Column(name = "COD_RES_RES", nullable = false, unique = true)
        private String code;
        private String ipfsUrl;
        @Column(name = "LIB_RES_RES", nullable = false)
        private String libelle;

        @Column(name = "COD_TYP_RESS")
        private String typeRessource;
        @Column(name = "TEMPS_ESTIME_JOURS")
        private Double tempsEstimeJours;


        @Column(name = "BOOL_STAT_RES")
        private boolean statut;
        @ManyToOne
        @JoinColumn(name = "COD_APP_APP", referencedColumnName = "COD_APP_APP")
        private Application application;

        @JsonIgnoreProperties("ressources") // Solution alternative

// relation inverse (facultative si bidirectionnelle)
        @ManyToMany(mappedBy = "ressources")
        private Set<Profil> profils;
}
