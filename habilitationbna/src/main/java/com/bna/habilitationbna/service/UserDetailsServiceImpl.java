package com.bna.habilitationbna.service;
import com.bna.habilitationbna.model.User;

import com.bna.habilitationbna.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String matricule) throws UsernameNotFoundException {
        User user = userRepository.findByMatricule(matricule)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + matricule));

        Set<SimpleGrantedAuthority> authorities = user.getProfils().stream()
                .map(profil -> new SimpleGrantedAuthority(profil.getRole())) // Convertit directement le nom du profil en SimpleGrantedAuthority
                .collect(Collectors.toSet());

        return new org.springframework.security.core.userdetails.User(
                user.getMatricule(),
                user.getPassword(),
                authorities
        );
    }
}

