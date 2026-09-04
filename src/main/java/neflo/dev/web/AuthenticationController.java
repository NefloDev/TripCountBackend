package neflo.dev.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neflo.dev.exceptions.AuthenticationException;
import neflo.dev.model.dto.GoogleLoginDTO;
import neflo.dev.model.dto.LoginResponse;
import neflo.dev.model.dto.user.UserDTO;
import neflo.dev.model.dto.user.UserLoginDTO;
import neflo.dev.model.entity.UserModel;
import neflo.dev.service.authentication.AuthenticationService;
import neflo.dev.service.authentication.GoogleAuthenticationService;
import neflo.dev.service.authentication.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/auth")
public class AuthenticationController {

    private final JwtService jwtService;
    private final AuthenticationService authenticationService;
    private final GoogleAuthenticationService googleAuthenticationService;

    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> registerUser(@RequestBody UserDTO userDTO) {
        if (authenticationService.isUserRegistered(userDTO)) {
            throw new AuthenticationException("user-registered", "That email is already in use");
        }

        UserModel registeredUser = authenticationService.signup(userDTO);
        String jwtToken = jwtService.generateToken(registeredUser);

        LoginResponse loginResponse = new LoginResponse(jwtToken, jwtService.getExpirationTime());

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@RequestBody UserLoginDTO loginDTO) {
        UserModel authenticatedUser = authenticationService.authenticate(loginDTO);

        String jwtToken = jwtService.generateToken(authenticatedUser);

        LoginResponse loginResponse = new LoginResponse(jwtToken, jwtService.getExpirationTime());

        return ResponseEntity.ok(loginResponse);
    }

    @GetMapping("/refresh")
    public ResponseEntity<LoginResponse> authenticateUser(@AuthenticationPrincipal UserModel user) {
        String jwtToken = jwtService.generateToken(user);

        LoginResponse loginResponse = new LoginResponse(jwtToken, jwtService.getExpirationTime());

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/google/login")
    public ResponseEntity<LoginResponse> authenticateGoogleUser(@RequestBody GoogleLoginDTO request) {
        return ResponseEntity.ok(googleAuthenticationService.authenticate(request.idToken()));
    }

}
