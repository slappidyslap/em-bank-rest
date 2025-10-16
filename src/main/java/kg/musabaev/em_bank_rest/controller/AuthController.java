package kg.musabaev.em_bank_rest.controller;

import jakarta.validation.Valid;
import kg.musabaev.em_bank_rest.dto.AuthenticateRefreshUserResponse;
import kg.musabaev.em_bank_rest.dto.AuthenticateRequest;
import kg.musabaev.em_bank_rest.dto.SignupUserRequest;
import kg.musabaev.em_bank_rest.dto.SignupUserResponse;
import kg.musabaev.em_bank_rest.security.JwtUtil;
import kg.musabaev.em_bank_rest.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignupUserResponse> signup(@Valid @RequestBody SignupUserRequest dto) {
            return ResponseEntity.ok(authService.signup(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticateRefreshUserResponse> login(@Valid @RequestBody AuthenticateRequest dto) {
        return ResponseEntity.ok(authService.login(dto));
    }
}
