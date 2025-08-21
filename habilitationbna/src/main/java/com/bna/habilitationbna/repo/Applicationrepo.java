package com.bna.habilitationbna.repo;

import com.bna.habilitationbna.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Applicationrepo extends JpaRepository <Application, String> {
    boolean existsByCode(String code);

}
