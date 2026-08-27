package neflo.dev.service.authentication;

import lombok.extern.slf4j.Slf4j;
import neflo.dev.exceptions.AuthenticationException;
import neflo.dev.exceptions.ValidationException;
import neflo.dev.model.dto.user.UserDTO;
import neflo.dev.model.dto.user.UserLoginDTO;
import neflo.dev.model.entity.UserModel;
import neflo.dev.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public boolean isUserRegistered(UserDTO userDTO) {
        return userRepository.existsByEmail(userDTO.email());
    }

    public UserModel signup(UserDTO userDTO) {
        if (userDTO.password().isEmpty()) {
            throw new ValidationException("empty-password", "Password field is empty.");
        }

        UserModel user = UserModel.builder()
                .email(userDTO.email())
                .name(userDTO.name())
                .nickname(userDTO.nickname())
                .password(passwordEncoder.encode(userDTO.password().get()))
                .build();

        if (userDTO.pfp().isPresent()) {
            user.setPfp(userDTO.pfp().get());
        }

        return userRepository.save(user);
    }

    public UserModel authenticate(UserLoginDTO userLoginDTO) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userLoginDTO.email(), userLoginDTO.password())
            );
        } catch (Exception e) {
            throw new AuthenticationException("user-authentication-exception", "There was an issue authenticating the current user, try again later.");
        }

        return userRepository.findByEmail(userLoginDTO.email()).orElseThrow();
    }

}
