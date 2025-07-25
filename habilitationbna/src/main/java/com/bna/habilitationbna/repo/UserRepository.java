package com.bna.habilitationbna.repo;

import com.bna.habilitationbna.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByMatricule(String matricule); // 👈 ajoute ceci
    //void deleteByMatricule(String matricule);  // <-- ajoute cette méthode
    @Modifying
    @Query("DELETE FROM User u WHERE u.matricule = :matricule")
    int deleteByMatricule(@Param("matricule") String matricule);
}
