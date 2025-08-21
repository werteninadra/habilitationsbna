package com.bna.habilitationbna.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String matricule;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String telephone;

    @Column( nullable = false)
    @ColumnDefault("true")
    private Boolean active = true;

    @Column( nullable = false)
    @ColumnDefault("false")
    private Boolean blocked = false;

    @Column()
    private String profileImagePath;

    @Column()
    private Instant lastLogin;

    @Column( nullable = false)
    @ColumnDefault("false")
    private Boolean loggedIn = false;
    //@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            joinColumns = @JoinColumn,
            inverseJoinColumns = @JoinColumn
    )
    private Set<Profil> profils = new HashSet<>();

    // Business methods
    public void activate() {
        this.active = true;
        this.blocked = false;
        this.lastLogin = Instant.now();
    }

    public void logout() {
        this.loggedIn = false;
        this.lastLogin = Instant.now();
    }




    @ManyToOne
    @JoinColumn
    @JsonBackReference
    private Agence agence;

    // Ensure default values are set before persisting
    @PrePersist
    public void setDefaultValues() {
        if (this.active == null) {
            this.active = true;
        }
        if (this.blocked == null) {
            this.blocked = false;
        }
        if (this.loggedIn == null) {
            this.loggedIn = false;
        }
    }
}