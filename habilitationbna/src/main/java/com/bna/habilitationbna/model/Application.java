package com.bna.habilitationbna.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "applications")
@Getter
@Setter
public class Application {
    @Id
    @Column(name = "COD_APP_APP", nullable = false, unique = true)
    private String code;

    @Column(name = "LIB_APP_APP", nullable = false)
    private String libelle;

    @Column(name = "DESC_APP_APP")
    private String description;

    @OneToMany(mappedBy = "application", fetch = FetchType.LAZY)
    @JsonIgnore // Ajoutez cette annotation pour éviter les références circulaires
    private Set<Ressource> ressources = new HashSet<>(); //
}