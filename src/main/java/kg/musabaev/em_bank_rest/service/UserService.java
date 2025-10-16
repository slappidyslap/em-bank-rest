package kg.musabaev.em_bank_rest.service;

import kg.musabaev.em_bank_rest.dto.PatchUserRequest;
import kg.musabaev.em_bank_rest.entity.User;
import kg.musabaev.em_bank_rest.repository.specification.UserSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    User getById(Long id);

    Page<User> getAll(UserSpecification filter, Pageable pageable);

    void delete(Long id);

    User patch(Long id, PatchUserRequest dto);
}
