package com.bna.habilitationbna.service;

import com.bna.habilitationbna.model.Agence;
import com.bna.habilitationbna.model.Occupation;
import com.bna.habilitationbna.repo.AgenceRepository;
import com.bna.habilitationbna.repo.OccupationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class OccupationService {

    private final OccupationRepository occupationRepository;
    private final AgenceRepository agenceRepository;

    public OccupationService(OccupationRepository occupationRepository, AgenceRepository agenceRepository) {
        this.occupationRepository = occupationRepository;
        this.agenceRepository = agenceRepository;
    }

    public Occupation createOccupation(Long agenceId, int nombreClients, boolean estFerie, String meteo, int jourSemaine) {
        Agence agence = agenceRepository.findById(agenceId)
                .orElseThrow(() -> new RuntimeException("Agence non trouvée"));

        Occupation occupation = new Occupation();
        occupation.setAgence(agence);
        occupation.setDate(LocalDate.now()); // <-- date du jour fixée ici
        occupation.setNombreClients(nombreClients);
        occupation.setEstFerie(estFerie);
        occupation.setMeteo(meteo);
        occupation.setJourSemaine(jourSemaine);
        // le tauxOccupation sera calculé dans @PrePersist / @PreUpdate si tu as bien ta méthode

        return occupationRepository.save(occupation);
    }
}
