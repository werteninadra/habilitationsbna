package com.bna.habilitationbna.service;



import com.bna.habilitationbna.model.Agence;
        import com.bna.habilitationbna.model.DetailsAgence;

        import java.util.List;

public interface IAgenceService {

    List<Agence> findAll();
    DetailsAgence getDetails(Long id);
    Agence findById(Long id);
    Agence save(Agence agence);
    void delete(Long id);
}
