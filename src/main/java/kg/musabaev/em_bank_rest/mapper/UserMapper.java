package kg.musabaev.em_bank_rest.mapper;

import kg.musabaev.em_bank_rest.dto.*;
import kg.musabaev.em_bank_rest.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface UserMapper {

    User toEntity(SignupUserRequest signupUserRequest);

    SignupUserRequest toSignupUserRequest(User user);

    GetCreatePatchUserResponse toGetUserResponse(User user);

    GetCreatePatchUserResponse toCreateUserResponse(User user);

    GetCreatePatchUserResponse toPatchUserResponse(User user);

    void patch(PatchUserRequest dto, @MappingTarget User entity);
}