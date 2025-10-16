package kg.musabaev.em_bank_rest.security;

import kg.musabaev.em_bank_rest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SimpleUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        return userRepository
                .findByEmail(email)
                .map(SimpleUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User by " + email + " email not found"));
    }
}
