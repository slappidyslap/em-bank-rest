package kg.musabaev.em_bank_rest.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import kg.musabaev.em_bank_rest.dto.GetCreatePatchUserResponse;
import kg.musabaev.em_bank_rest.dto.PatchUserRequest;
import kg.musabaev.em_bank_rest.repository.specification.UserSpecification;
import kg.musabaev.em_bank_rest.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Valid
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public PagedModel<GetCreatePatchUserResponse> getAllUser(
            @ModelAttribute UserSpecification filters, Pageable pageable) {
        return userService.getAll(filters, pageable);
    }

    @GetMapping("/{userId}")
    public GetCreatePatchUserResponse getUserById(@Positive @PathVariable Long userId) {
        return userService.getByIdForAdmin(userId);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@Positive @PathVariable Long userId) {
        userService.delete(userId);
    }

    @PatchMapping("/{userId}")
    public GetCreatePatchUserResponse updateUser(
            @Positive @PathVariable Long userId,
            @Valid @RequestBody PatchUserRequest dto) {
        return userService.patchForAdmin(userId, dto);
    }
}
