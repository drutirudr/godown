package com.shyam.kamak.godown.controller;

import com.shyam.kamak.godown.dto.AuthRequest;
import com.shyam.kamak.godown.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/bundles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @PostMapping("/api/public/register")
    public ResponseEntity<String> register(@RequestBody AuthRequest authRequest) {
//        // Hash the raw password before saving it to the database
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
//        userRepository.save(user);
//        return "User registered successfully!";
        return ResponseEntity.ok(userService.registerNewUser(authRequest));
    }
}
