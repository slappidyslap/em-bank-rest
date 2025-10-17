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

    @GetMapping("/{id}")
    public ResponseEntity<GetCreatePatchUserResponse> getUserById(@Positive @PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@Positive @PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GetCreatePatchUserResponse> updateUser(
            @Positive @PathVariable Long id,
            @Valid @RequestBody PatchUserRequest dto) {
        return ResponseEntity.ok(userService.patch(id, dto));
    }
}
