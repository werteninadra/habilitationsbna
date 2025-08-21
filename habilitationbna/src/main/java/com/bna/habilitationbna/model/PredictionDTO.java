package com.bna.habilitationbna.model;
// PredictionDTO.java

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
//@AllArgsConstructor


public class PredictionDTO {
    private LocalDate datePic;
    private Double tauxPrediction;
    private List<String> suggestions;

    // Constructeur, getters et setters
    public PredictionDTO(LocalDate datePic, Double tauxPrediction, List<String> suggestions) {
        this.datePic = datePic;
        this.tauxPrediction = tauxPrediction;
        this.suggestions = suggestions;
    }

    // Getters et setters...
}