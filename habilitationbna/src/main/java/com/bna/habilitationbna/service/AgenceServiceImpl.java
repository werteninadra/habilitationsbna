package com.bna.habilitationbna.service;

import com.bna.habilitationbna.model.Agence;
import com.bna.habilitationbna.model.DetailsAgence;
import com.bna.habilitationbna.repo.AgenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgenceServiceImpl implements IAgenceService {

    private final AgenceRepository agenceRepository;




    @Override
    public List<Agence> findAll() {
        return agenceRepository.findAll();
    }

    @Override
    public DetailsAgence getDetails(Long id) {
        return agenceRepository.findDetailsByAgenceId(id)
                .orElseThrow(() -> new RuntimeException("Détails de l'agence non trouvés pour l'ID: " + id));
    }

    @Override
    public Agence findById(Long id) {
        return agenceRepository.findById(id).orElse(null);
    }

    @Override
    public Agence save(Agence agence) {
        return agenceRepository.save(agence);
    }

    @Override
    public void delete(Long id) {
        agenceRepository.deleteById(id);
    }
}