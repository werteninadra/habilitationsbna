package com.bna.habilitationbna.repo;

import com.bna.habilitationbna.model.Ressource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RessourceRepository extends JpaRepository<Ressource, String> {
}