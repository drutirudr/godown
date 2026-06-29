package com.shyam.kamak.godown.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor // <-- This must be present
public class ErrorResponse {
    private int status;
    private String message;
}
