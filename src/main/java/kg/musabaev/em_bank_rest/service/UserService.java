package kg.musabaev.em_bank_rest.service;

import kg.musabaev.em_bank_rest.dto.GetCreatePatchUserResponse;
import kg.musabaev.em_bank_rest.dto.PatchUserRequest;
import kg.musabaev.em_bank_rest.entity.User;
import kg.musabaev.em_bank_rest.repository.specification.UserSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    GetCreatePatchUserResponse getById(Long id);

    Page<GetCreatePatchUserResponse> getAll(UserSpecification filter, Pageable pageable);

    void delete(Long id);

    GetCreatePatchUserResponse patch(Long id, PatchUserRequest dto);
}
