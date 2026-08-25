package org.example.marketplace.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.marketplace.dto.auth.RegisterRequest;
import org.example.marketplace.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class RegisterController {
    private final AuthService service;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void doRegister(@RequestBody @Valid RegisterRequest request){
        service.doRegister(request);
    }
}
