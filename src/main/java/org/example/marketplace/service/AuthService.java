package org.example.marketplace.service;

import lombok.AllArgsConstructor;
import org.example.marketplace.dto.auth.RegisterRequest;
import org.example.marketplace.entity.User;
import org.example.marketplace.entity.UserRole;
import org.example.marketplace.exception.AuthUserAlreadyException;
import org.example.marketplace.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AuthService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void doRegister(RegisterRequest request){
        String username = request.username();
        String password = request.password();

        if(repository.findByUsername(username).isPresent()){
            throw new AuthUserAlreadyException("Данный пользователь %s уже существует".formatted(username));
        }

        String hashPass = passwordEncoder.encode(password);

        User user = new User(username, hashPass, UserRole.USER);

        repository.save(user);
    }
}
