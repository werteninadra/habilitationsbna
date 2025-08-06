package com.bna.habilitationbna.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class OccupationHistorique {
    private LocalDate date;
    private Double taux;
    private Integer nombreClients;
}