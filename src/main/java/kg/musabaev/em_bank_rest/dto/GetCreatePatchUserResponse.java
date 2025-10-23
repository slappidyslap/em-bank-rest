package kg.musabaev.em_bank_rest.dto;

import kg.musabaev.em_bank_rest.security.Role;
import lombok.Builder;

@Builder
public record GetCreatePatchUserResponse(Long id, String fullName, String email, Role role) {
}