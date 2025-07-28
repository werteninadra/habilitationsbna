package com.bna.habilitationbna.repo;

import com.bna.habilitationbna.model.Profil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface ProfilRepository extends JpaRepository<Profil, String> { // String comme type d'ID

    Optional<Profil> findByNom(String nom);

    Set<Profil> findByNomIn(Set<String> noms);
}