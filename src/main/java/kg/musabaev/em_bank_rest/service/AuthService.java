package kg.musabaev.em_bank_rest.service;

import kg.musabaev.em_bank_rest.dto.AuthenticateRefreshUserResponse;
import kg.musabaev.em_bank_rest.dto.AuthenticateRequest;
import kg.musabaev.em_bank_rest.dto.SignupUserRequest;
import kg.musabaev.em_bank_rest.dto.SignupUserResponse;

public interface AuthService {
    SignupUserResponse signup(SignupUserRequest dto);

    AuthenticateRefreshUserResponse login(AuthenticateRequest dto);

    AuthenticateRefreshUserResponse refresh(/*TODO*/);
}
