package com.danone.pdpbackend.Services.Implimetations;

import com.danone.pdpbackend.Controller.auth.*;
import com.danone.pdpbackend.Repo.UsersRepo;
import com.danone.pdpbackend.Services.AuthenticationService;
import com.danone.pdpbackend.Utils.Roles;
import com.danone.pdpbackend.Utils.exceptions.UserAlreadyExistsException;
import com.danone.pdpbackend.config.JwtService;
import com.danone.pdpbackend.entities.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UsersRepo usersRepo;

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;


    @Override
    public RegisterResponse register(RegisterRequest registerRequest) {

        Boolean existsUserByUsername = usersRepo.existsUserByUsername(registerRequest.getUsername());
        if (existsUserByUsername) {
            log.error("User with username {} already exists", registerRequest.getUsername());
            throw new UserAlreadyExistsException("User already exists");
        }



        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        User user = User.builder().username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(Roles.USER)
                .build();

        usersRepo.save(user);

        String userToken = jwtService.generateToken(user);
        return RegisterResponse.builder().token(userToken).build();
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword()));
        User user = usersRepo.findByUsername(authenticationRequest.getUsername()).orElseThrow();
        String userToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder().token(userToken).user(user).build();
    }


    @Override
    public User getUser(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7); // Remove "Bearer "
        }

        String username = jwtService.extractUsernameFromToken(token);
        return usersRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

}
