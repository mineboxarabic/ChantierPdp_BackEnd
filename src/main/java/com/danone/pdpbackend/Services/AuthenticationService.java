package com.danone.pdpbackend.Services;

import com.danone.pdpbackend.Controller.auth.AuthenticationRequest;
import com.danone.pdpbackend.Controller.auth.AuthenticationResponse;
import com.danone.pdpbackend.Controller.auth.RegisterRequest;
import com.danone.pdpbackend.Controller.auth.RegisterResponse;
import com.danone.pdpbackend.entities.User;

public interface AuthenticationService {
    RegisterResponse register(RegisterRequest registerRequest);

    AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest);

    User getUser(String token);
}
