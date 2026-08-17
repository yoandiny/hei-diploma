package mg.yoan.diploma.endpoint.rest.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import mg.yoan.diploma.repository.JUserRepository;
import mg.yoan.diploma.repository.model.JUser;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtService jwtService;
  private final JwtCookie jwtCookie;
  private final JUserRepository userRepository;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String token = bearerToken(request).or(() -> jwtCookie.read(request)).orElse(null);
    if (token == null) {
      filterChain.doFilter(request, response);
      return;
    }

    Optional<Claims> claims = jwtService.parseClaims(token);
    if (claims.isPresent() && SecurityContextHolder.getContext().getAuthentication() == null) {
      Claims c = claims.get();
      Optional<JUser> stored =
          userRepository.findById(jwtService.extractUserId(c)).filter(JUser::isEnabled);
      if (stored.isEmpty()) {
        jwtCookie.clear(request, response);
        filterChain.doFilter(request, response);
        return;
      }
      JUser user = stored.get();
      AuthenticatedUser principal =
          new AuthenticatedUser(
              user.getId(),
              user.getEmail(),
              user.getRole(),
              user.getFirstName(),
              user.getLastName());

      var authority = new SimpleGrantedAuthority("ROLE_" + principal.getRole().name());
      var authentication =
          new UsernamePasswordAuthenticationToken(principal, null, List.of(authority));

      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }

  private Optional<String> bearerToken(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      return Optional.empty();
    }
    return Optional.of(authHeader.substring(BEARER_PREFIX.length()));
  }
}
