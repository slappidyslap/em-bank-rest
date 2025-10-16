package kg.musabaev.em_bank_rest.service;

import kg.musabaev.em_bank_rest.dto.PatchUserRequest;
import kg.musabaev.em_bank_rest.entity.User;
import kg.musabaev.em_bank_rest.repository.specification.UserSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface UserService {

    ResponseEntity<User> getById(Long id);

    ResponseEntity<Page<User>> getAll(UserSpecification filter, Pageable pageable);

    ResponseEntity<?> delete(Long id);

    ResponseEntity<User> patch(Long id, PatchUserRequest dto);
}
