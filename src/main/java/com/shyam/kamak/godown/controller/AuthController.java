package com.shyam.kamak.godown.controller;

import com.shyam.kamak.godown.dto.AuthResponse;
import com.shyam.kamak.godown.security.JwtService;
import com.shyam.kamak.godown.service.UserService;
import com.shyam.kamak.godown.dto.AuthRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;

@Slf4j
@RestController
@RequestMapping("/api/auth")
//@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthController(UserService userService, JwtService jwtService,
                          AuthenticationManager authenticationManager, UserDetailsService userDetailsService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

//    public AuthController(UserService userService) {
//        this.userService = userService;
//    }

    @PostMapping("/register")
    //public ResponseEntity<String> registerUser(@RequestBody RegisterRequest request) {
    public ResponseEntity<String> registerUser(@Valid @RequestBody AuthRequest request) {
        try {
            String message = userService.registerNewUser(request);
            return new ResponseEntity<>(message, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody AuthRequest request) {
        // 1. Authenticate credentials. If wrong, Spring automatically throws a BadCredentialsException

        log.info("User {}, pwd {}, role {}", request.username(), request.password(), request.role());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        // 2. Load the authenticated user profile from the database
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());

        // 3. Generate the token string
        final String jwtToken = jwtService.generateToken(userDetails);

        // 4. Return token inside the response envelope
        return ResponseEntity.ok(new AuthResponse(jwtToken));
    }
}
