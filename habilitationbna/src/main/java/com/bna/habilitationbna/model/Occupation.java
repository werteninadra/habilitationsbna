package com.bna.habilitationbna.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Occupation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private double taux;
    private int nombreClients;

    @ManyToOne
    private Agence agence;

    // getters/setters
}
