package com.bna.habilitationbna.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Agence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numeroAgence;
    private String nom;
    private String adresse;
    private String ville;

    private Double latitude;   // <-- ajoute ces champs

    private Double longitude;  // <-- ajoute ces champs


    @OneToOne
    @JoinColumn(name = "chef_agence_id")
    private User chefAgence;

    @OneToMany(mappedBy = "agence", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<User> employes = new ArrayList<>();

    private Integer capaciteMax;
    private LocalDate dateCreation;
}