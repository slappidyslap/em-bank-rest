package kg.musabaev.em_bank_rest.service;

import kg.musabaev.em_bank_rest.dto.*;

public interface AuthService {

    GetCreatePatchUserResponse signup(SignupUserRequest dto);

    AccessAndRefreshTokensResponse login(AuthenticateRequest dto);

    AccessAndRefreshTokensResponse refresh(String refreshToken);

    void revokeAllUserRefreshTokens(Long userId);
}
