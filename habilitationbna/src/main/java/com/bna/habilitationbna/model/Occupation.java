package com.bna.habilitationbna.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Occupation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private int nombreClients;

    private double tauxOccupation; // (nombreClients / capaciteMax) * 100

    // Ajout des champs pour la prédiction
    private int jourSemaine;  // 0 (lundi) à 6 (dimanche)
    private boolean estFerie; // true si jour férié
    private String meteo;     // ex: "soleil", "pluie", "nuageux"...

    @ManyToOne
    @JoinColumn( nullable = false)
    private Agence agence;

    @PrePersist
    @PreUpdate
    public void calculerTauxOccupation() {
        if (agence != null && agence.getCapaciteMax() != null && agence.getCapaciteMax() > 0) {
            double taux = ((double) nombreClients / agence.getCapaciteMax()) * 100;
            this.tauxOccupation = Math.min(taux, 100.0);  // Limite à 100%
        } else {
            this.tauxOccupation = 0;
        }
    }


}
