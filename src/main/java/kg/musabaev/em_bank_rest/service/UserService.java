package kg.musabaev.em_bank_rest.service;

import jakarta.validation.Valid;
import kg.musabaev.em_bank_rest.dto.GetCreatePatchUserResponse;
import kg.musabaev.em_bank_rest.dto.PatchUserRequest;
import kg.musabaev.em_bank_rest.dto.UpdatePasswordRequest;
import kg.musabaev.em_bank_rest.repository.specification.UserSpecification;
import kg.musabaev.em_bank_rest.security.SimpleUserDetails;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;

public interface UserService {

    GetCreatePatchUserResponse getByIdForAdmin(Long id);

    GetCreatePatchUserResponse getByIdForUser(SimpleUserDetails userDetails);

    PagedModel<GetCreatePatchUserResponse> getAll(UserSpecification filter, Pageable pageable);

    void delete(Long id);

    GetCreatePatchUserResponse patchForAdmin(Long id, PatchUserRequest dto);

    GetCreatePatchUserResponse patchForUser(PatchUserRequest dto, SimpleUserDetails userDetails);

    void updatePassword(UpdatePasswordRequest dto, SimpleUserDetails userDetails);
}
