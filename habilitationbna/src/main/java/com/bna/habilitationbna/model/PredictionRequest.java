package com.bna.habilitationbna.model;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor// PredictionRequest.java


public class PredictionRequest {
    private List<Map<String, Object>> historique;
    private int jours;

    // getters & setters
}
