package com.bna.habilitationbna.repo;

import com.bna.habilitationbna.model.Occupation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OccupationRepository extends JpaRepository<Occupation, Long> {
    List<Occupation> findByAgenceIdOrderByDateAsc(Long agenceId);
}
