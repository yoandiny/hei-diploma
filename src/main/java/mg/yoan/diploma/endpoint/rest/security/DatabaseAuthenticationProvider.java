package mg.yoan.diploma.endpoint.rest.security;

import java.util.List;
import lombok.RequiredArgsConstructor;
import mg.yoan.diploma.repository.JUserRepository;
import mg.yoan.diploma.repository.model.JUser;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseAuthenticationProvider implements AuthenticationProvider {

  private final JUserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String email = authentication.getName();
    String password =
        authentication.getCredentials() == null ? "" : authentication.getCredentials().toString();

    try {
      JUser user =
          userRepository
              .findByEmail(email)
              .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

      if (!user.isEnabled()) {
        throw new DisabledException("Account is disabled");
      }
      if (!passwordEncoder.matches(password, user.getPassword())) {
        throw new BadCredentialsException("Invalid credentials");
      }

      AuthenticatedUser principal =
          new AuthenticatedUser(
              user.getId(),
              user.getEmail(),
              user.getRole(),
              user.getFirstName(),
              user.getLastName());

      return new UsernamePasswordAuthenticationToken(
          principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
    } catch (AuthenticationException exception) {
      throw exception;
    } catch (RuntimeException exception) {
      throw new InternalAuthenticationServiceException("Authentication failed", exception);
    }
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
