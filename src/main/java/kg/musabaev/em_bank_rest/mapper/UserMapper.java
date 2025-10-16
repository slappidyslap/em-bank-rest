package kg.musabaev.em_bank_rest.mapper;

import kg.musabaev.em_bank_rest.dto.SignupUserRequest;
import kg.musabaev.em_bank_rest.dto.SignupUserResponse;
import kg.musabaev.em_bank_rest.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface UserMapper {

    User toEntity(SignupUserRequest signupUserRequest);

    SignupUserRequest toSignupUserRequest(User user);

    SignupUserResponse toSignupUserResponse(User user);
}