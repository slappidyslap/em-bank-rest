package kg.musabaev.em_bank_rest.service;

import kg.musabaev.em_bank_rest.dto.GetCreatePatchUserResponse;
import kg.musabaev.em_bank_rest.dto.PatchUserRequest;
import kg.musabaev.em_bank_rest.repository.specification.UserSpecification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.security.core.Authentication;

public interface UserService {

    GetCreatePatchUserResponse getById(Long id);

    PagedModel<GetCreatePatchUserResponse> getAll(UserSpecification filter, Pageable pageable);

    void delete(Long id);

    GetCreatePatchUserResponse patch(Long id, PatchUserRequest dto);

    GetCreatePatchUserResponse getById(Authentication authUser);

    GetCreatePatchUserResponse patch(PatchUserRequest dto, Authentication authUser);
}
