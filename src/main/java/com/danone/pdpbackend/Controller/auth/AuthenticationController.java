package com.danone.pdpbackend.Controller.auth;


import com.danone.pdpbackend.Services.AuthenticationService;
import com.danone.pdpbackend.Utils.ApiResponse;
import com.danone.pdpbackend.Utils.exceptions.UserAlreadyExistsException;
import com.danone.pdpbackend.entities.User;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;


    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @RequestBody RegisterRequest registerRequest
    ) {
        log.info("Register request: {}", registerRequest);
        RegisterResponse response = authenticationService.register(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(response, "User registered"));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> register(
            @RequestBody AuthenticationRequest authenticationRequest
    ){
        return ResponseEntity.ok(new ApiResponse<>(authenticationService.authenticate(authenticationRequest), "User authenticated"));
    }




    @GetMapping("/user")
    public ResponseEntity<User> getUser(@RequestHeader("Authorization") String token) {
        User user = authenticationService.getUser(token);
        return ResponseEntity.ok(user);
    }


    // Global exception handler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGlobalException(Exception e) {
        log.error("Unhandled exception: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(null, "An unexpected error occurred"));
    }


    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<String>> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        log.error("User registration failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>(null, ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<String>> handleBadCredentialsException(AuthenticationException ex) {
        log.error("User authentication failed1: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(null, ex.getMessage()));
    }




}
