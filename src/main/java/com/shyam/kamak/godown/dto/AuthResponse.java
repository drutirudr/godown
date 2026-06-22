package com.shyam.kamak.godown.dto;

public record AuthResponse(String token, String type) {
    public AuthResponse(String token) {
        this(token, "Bearer");
    }
}
