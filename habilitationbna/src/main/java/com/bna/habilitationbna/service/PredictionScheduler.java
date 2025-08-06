package com.bna.habilitationbna.service;

import com.bna.habilitationbna.model.Agence;
import com.bna.habilitationbna.repo.OccupationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PredictionScheduler {

    private final IAgenceService agenceService;
    private final PredictionService predictionService;
    private final OccupationRepository occupationRepository;

    public PredictionScheduler(
            IAgenceService agenceService,
            PredictionService predictionService,
            OccupationRepository occupationRepository
    ) {
        this.agenceService = agenceService;
        this.predictionService = predictionService;
        this.occupationRepository = occupationRepository;
    }

}
