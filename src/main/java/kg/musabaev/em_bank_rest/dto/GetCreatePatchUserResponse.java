package kg.musabaev.em_bank_rest.dto;

import kg.musabaev.em_bank_rest.security.Role;

public record GetCreatePatchUserResponse(Long id, String fullName, String email, Role role) {
}