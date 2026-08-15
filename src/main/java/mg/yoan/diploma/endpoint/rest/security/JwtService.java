package mg.yoan.diploma.endpoint.rest.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import mg.yoan.diploma.domain.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

  private static final String CLAIM_EMAIL = "email";
  private static final String CLAIM_ROLE = "role";
  private static final String CLAIM_FIRST_NAME = "firstName";
  private static final String CLAIM_LAST_NAME = "lastName";

  private final SecretKey signingKey;
  private final long expirationSeconds;

  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.expiration-seconds:86400}") long expirationSeconds) {
    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationSeconds = expirationSeconds;
  }

  public String generateToken(String userId, String email, Role role) {
    return generateToken(userId, email, role, null, null);
  }

  public String generateToken(
      String userId, String email, Role role, String firstName, String lastName) {
    Instant now = Instant.now();
    var builder =
        Jwts.builder()
            .subject(userId)
            .claim(CLAIM_EMAIL, email)
            .claim(CLAIM_ROLE, role.name())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(expirationSeconds)))
            .signWith(signingKey);
    if (firstName != null) {
      builder.claim(CLAIM_FIRST_NAME, firstName);
    }
    if (lastName != null) {
      builder.claim(CLAIM_LAST_NAME, lastName);
    }
    return builder.compact();
  }

  public long getExpirationSeconds() {
    return expirationSeconds;
  }

  public Optional<Claims> parseClaims(String token) {
    try {
      Claims claims =
          Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
      return Optional.of(claims);
    } catch (JwtException | IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  public String extractUserId(Claims claims) {
    return claims.getSubject();
  }

  public String extractEmail(Claims claims) {
    return claims.get(CLAIM_EMAIL, String.class);
  }

  public Role extractRole(Claims claims) {
    return Role.valueOf(claims.get(CLAIM_ROLE, String.class));
  }

  public String extractFirstName(Claims claims) {
    return claims.get(CLAIM_FIRST_NAME, String.class);
  }

  public String extractLastName(Claims claims) {
    return claims.get(CLAIM_LAST_NAME, String.class);
  }
}
