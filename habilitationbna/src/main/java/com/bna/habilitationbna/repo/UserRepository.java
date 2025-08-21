package com.bna.habilitationbna.repo;

import com.bna.habilitationbna.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByAgenceId(Long agenceId);

    Optional<User> findByMatricule(String matricule); // 👈 ajoute ceci
    //void deleteByMatricule(String matricule);  // <-- ajoute cette méthode

}
