package com.bna.habilitationbna.repo;

import com.bna.habilitationbna.model.Occupation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OccupationRepository extends JpaRepository<Occupation, Long> {
    List<Occupation> findByAgenceId(Long agenceId);
    List<Occupation> findByAgenceIdAndDate(Long agenceId, LocalDate date);


}
