package kg.musabaev.em_bank_rest.controller;

import jakarta.validation.Valid;
import kg.musabaev.em_bank_rest.dto.*;
import kg.musabaev.em_bank_rest.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public GetCreatePatchUserResponse signup(@Valid @RequestBody SignupUserRequest dto) {
            return authService.signup(dto);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticateUserResponse> login(@Valid @RequestBody AuthenticateRequest dto) {
        return getAuthenticateUserResponse(authService.login(dto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticateUserResponse> refresh(@CookieValue(name = "REFRESH_TOKEN") String refreshToken) {
        return getAuthenticateUserResponse(authService.refresh(refreshToken));
    }

    private ResponseEntity<AuthenticateUserResponse> getAuthenticateUserResponse(AccessAndRefreshTokensResponse tokens) {
        var refreshTokenCookie = ResponseCookie.from("REFRESH_TOKEN", tokens.refreshToken())
                .httpOnly(true)
                .path("/api/v1/auth/refresh")
                .build();
        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(new AuthenticateUserResponse(tokens.accessToken()));
    }
}
