package com.bna.habilitationbna.model;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    public List<Map<String, Object>> getHistorique() { return historique; }
    public void setHistorique(List<Map<String, Object>> historique) { this.historique = historique; }
    public int getJours() { return jours; }
    public void setJours(int jours) { this.jours = jours; }
}
