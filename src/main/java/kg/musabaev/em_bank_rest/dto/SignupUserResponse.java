package kg.musabaev.em_bank_rest.dto;

public record SignupUserResponse(
        Long id,
        String fullName,
        String email,
        String role) {
}