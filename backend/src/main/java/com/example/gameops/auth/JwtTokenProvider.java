package com.example.gameops.auth;

import com.example.gameops.admin.domain.Admin;
import com.example.gameops.admin.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  private static final String CLAIM_ROLE = "role";
  private static final String CLAIM_USERNAME = "username";
  private static final String CLAIM_DISPLAY_NAME = "displayName";
  private static final String CLAIM_TYPE = "type";
  private static final String TYPE_ACCESS = "access";
  private static final String TYPE_REFRESH = "refresh";

  private final SecretKey key;
  private final Duration accessExpiration;
  private final Duration refreshExpiration;

  public JwtTokenProvider(JwtProperties properties) {
    this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    this.accessExpiration = Duration.ofMinutes(properties.accessExpirationMinutes());
    this.refreshExpiration = Duration.ofDays(properties.refreshExpirationDays());
  }

  public String createAccessToken(Admin admin) {
    return buildToken(admin, accessExpiration, TYPE_ACCESS);
  }

  public String createRefreshToken(Admin admin) {
    return buildToken(admin, refreshExpiration, TYPE_REFRESH);
  }

  public AdminPrincipal parseAccessToken(String token) {
    Jws<Claims> jws = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    Claims claims = jws.getPayload();
    String type = claims.get(CLAIM_TYPE, String.class);
    if (!TYPE_ACCESS.equals(type)) {
      throw new IllegalArgumentException("not an access token");
    }
    Long adminId = Long.valueOf(claims.getSubject());
    String username = claims.get(CLAIM_USERNAME, String.class);
    Role role = Role.valueOf(claims.get(CLAIM_ROLE, String.class));
    String displayName = claims.get(CLAIM_DISPLAY_NAME, String.class);
    return new AdminPrincipal(adminId, username, role, displayName);
  }

  private String buildToken(Admin admin, Duration ttl, String type) {
    Date now = new Date();
    Date exp = new Date(now.getTime() + ttl.toMillis());
    return Jwts.builder()
        .subject(String.valueOf(admin.getId()))
        .claim(CLAIM_USERNAME, admin.getUsername())
        .claim(CLAIM_ROLE, admin.getRole().name())
        .claim(CLAIM_DISPLAY_NAME, admin.getDisplayName())
        .claim(CLAIM_TYPE, type)
        .issuedAt(now)
        .expiration(exp)
        .signWith(key, Jwts.SIG.HS256)
        .compact();
  }
}
