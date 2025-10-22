package kg.musabaev.em_bank_rest.controller;

import jakarta.validation.Valid;
import kg.musabaev.em_bank_rest.dto.GetCreatePatchUserResponse;
import kg.musabaev.em_bank_rest.dto.PatchUserRequest;
import kg.musabaev.em_bank_rest.dto.UpdatePasswordRequest;
import kg.musabaev.em_bank_rest.security.SimpleUserDetails;
import kg.musabaev.em_bank_rest.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
@Valid
public class UserController {

    private final UserService userService;

    @GetMapping
    public GetCreatePatchUserResponse getMe(
            @AuthenticationPrincipal SimpleUserDetails userDetails) {
        return userService.getByIdForUser(userDetails);
    }

    @PatchMapping
    public GetCreatePatchUserResponse updateUser(
            @Valid @RequestBody PatchUserRequest dto,
            @AuthenticationPrincipal SimpleUserDetails userDetails) {
        return userService.patchForUser(dto, userDetails);
    }

    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePassword(
            @Valid @RequestBody UpdatePasswordRequest dto,
            @AuthenticationPrincipal SimpleUserDetails userDetails) {
        userService.updatePassword(dto, userDetails);
    }
}