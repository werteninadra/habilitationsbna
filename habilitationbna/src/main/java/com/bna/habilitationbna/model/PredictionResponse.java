package com.bna.habilitationbna.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResponse {
    private LocalDate datePic;
    private Double tauxPrediction;
    private List<String> suggestions;
}