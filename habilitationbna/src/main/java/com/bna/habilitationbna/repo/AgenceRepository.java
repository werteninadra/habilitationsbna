package com.bna.habilitationbna.repo;

import com.bna.habilitationbna.model.Agence;
import com.bna.habilitationbna.model.DetailsAgence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AgenceRepository extends JpaRepository<Agence, Long> {
    Optional<Agence> findByNumeroAgence(String numeroAgence);
    //Optional <DetailsAgence> findDetailsByAgenceId(Long agenceId); // Changez le retour en Optional

    @Query("SELECT a FROM Agence a WHERE a.ville = :ville")
    List<Agence> findByVille(String ville);

    @Query("SELECT d FROM DetailsAgence d WHERE d.agence.id = :agenceId")
    Optional<DetailsAgence> findDetailsByAgenceId(@Param("agenceId") Long agenceId);
   // @Query("SELECT d FROM DetailsAgence d WHERE d.agence.id = :agenceId")

}