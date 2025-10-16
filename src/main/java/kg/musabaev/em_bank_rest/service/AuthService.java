package kg.musabaev.em_bank_rest.service;

import kg.musabaev.em_bank_rest.dto.*;

public interface AuthService {
    SignupUserResponse signup(SignupUserRequest dto);

    AuthenticateRefreshUserResponse login(AuthenticateRequest dto);

    AuthenticateRefreshUserResponse refresh(UpdateTokensRequest dto);
}
