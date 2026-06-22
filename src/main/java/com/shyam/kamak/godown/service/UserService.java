package com.shyam.kamak.godown.service;

import com.shyam.kamak.godown.dto.AuthRequest;
import com.shyam.kamak.godown.exception.UsernameAlreadyExistsException;
import com.shyam.kamak.godown.model.User;
import com.shyam.kamak.godown.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String registerNewUser(AuthRequest request) {
        // 1. Prevent duplicate usernames
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new UsernameAlreadyExistsException("Username already exists!");
        }

        // 2. Map DTO fields to the database Entity model
        User newUser = new User();
        newUser.setUsername(request.username());

        // 3. SECURE HASHING: Convert raw text to BCrypt before hitting DB
        //newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setPassword(request.password());

        // 4. Default to standard USER role if none specified
        if (request.role() == null || request.role().trim().isEmpty()) {
            newUser.setRole("USER");
        } else {
            newUser.setRole(request.role().toUpperCase());
        }

        userRepository.save(newUser);
        return "User registered successfully!";
    }
}
