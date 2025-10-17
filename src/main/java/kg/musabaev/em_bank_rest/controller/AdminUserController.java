package kg.musabaev.em_bank_rest.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import kg.musabaev.em_bank_rest.dto.GetCreatePatchUserResponse;
import kg.musabaev.em_bank_rest.dto.PatchUserRequest;
import kg.musabaev.em_bank_rest.repository.specification.UserSpecification;
import kg.musabaev.em_bank_rest.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Valid
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<PagedModel<GetCreatePatchUserResponse>> getAllUser(
            @ModelAttribute UserSpecification filters, Pageable pageable) {
        return ResponseEntity.ok(userService.getAll(filters, pageable));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<GetCreatePatchUserResponse> getUserById(@Positive @PathVariable Long userId) {
        return ResponseEntity.ok(userService.getById(userId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@Positive @PathVariable Long userId) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<GetCreatePatchUserResponse> updateUser(
            @Positive @PathVariable Long userId,
            @Valid @RequestBody PatchUserRequest dto) {
        return ResponseEntity.ok(userService.patch(userId, dto));
    }
}
