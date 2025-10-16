package kg.musabaev.em_bank_rest.controller;

import jakarta.validation.Valid;
import kg.musabaev.em_bank_rest.dto.*;
import kg.musabaev.em_bank_rest.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<GetCreatePatchUserResponse> signup(@Valid @RequestBody SignupUserRequest dto) {
            return ResponseEntity.ok(authService.signup(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticateRefreshUserResponse> login(@Valid @RequestBody AuthenticateRequest dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticateRefreshUserResponse> refresh(@Valid @RequestBody UpdateTokensRequest dto) {
        return ResponseEntity.ok(authService.refresh(dto));
    }
}
