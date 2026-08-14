package mg.yoan.diploma.endpoint.rest.controller.auth;

import lombok.AllArgsConstructor;
import mg.yoan.diploma.endpoint.rest.security.JwtService;
import mg.yoan.diploma.repository.JUserRepository;
import mg.yoan.diploma.repository.model.JUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class AuthController {

  private final JUserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  @PostMapping("/auth/login")
  public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
    JUser user =
        userRepository
            .findByEmail(request.email())
            .filter(JUser::isEnabled)
            .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
            .orElse(null);

    if (user == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());

    return ResponseEntity.ok(
        new LoginResponse(
            token, "Bearer", jwtService.getExpirationSeconds(), user.getId(), user.getRole()));
  }
}
