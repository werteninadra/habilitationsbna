package com.bna.habilitationbna.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetailsAgence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "agence_id")
    private Agence agence;

    private Integer nombreClients;
    private Double tauxOccupation;

    @ElementCollection
    @CollectionTable(name = "agence_historique", joinColumns = @JoinColumn(name = "details_id"))
    private List<OccupationHistorique> historique = new ArrayList<>();
}

// OccupationHistorique.jav