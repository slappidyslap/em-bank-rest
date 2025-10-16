package kg.musabaev.em_bank_rest.controller;

import jakarta.validation.Valid;
import kg.musabaev.em_bank_rest.dto.GetCreatePatchUserResponse;
import kg.musabaev.em_bank_rest.dto.PatchUserRequest;
import kg.musabaev.em_bank_rest.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
@Valid
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<GetCreatePatchUserResponse> getUserById(
            @AuthenticationPrincipal Authentication auth) {
        return ResponseEntity.ok(userService.getById(auth));
    }

    @PatchMapping
    public ResponseEntity<GetCreatePatchUserResponse> updateUser(
            @Valid @RequestBody PatchUserRequest dto,
            @AuthenticationPrincipal Authentication auth) {
        return ResponseEntity.ok(userService.patch(dto, auth));
    }
}