package org.example.marketplace.controller.auth;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.marketplace.dto.auth.RegisterRequest;
import org.example.marketplace.dto.auth.jwt.LoginRequest;
import org.example.marketplace.dto.auth.jwt.LoginResponse;
import org.example.marketplace.service.auth.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService service;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void doRegister(@RequestBody @Valid RegisterRequest request){
        service.doRegister(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request){
        return service.login(request);
    }
}
