package com.mygomi.backend.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {
    // 0.12.x 부터는 키 생성을 위해 충분히 긴 문자열이 필요합니다.
    private final String secretKeyRaw = "mygomi-backend-secret-key-should-be-very-long-123456";
    private final SecretKey key = Keys.hmacShaKeyFor(secretKeyRaw.getBytes(StandardCharsets.UTF_8));
    private final long validityInMilliseconds = 3600000 * 24; // 24시간

    public String generateToken(String email) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .subject(email)                  // 0.12.x 문법: setSubject -> subject
                .issuedAt(now)                   // setIssuedAt -> issuedAt
                .expiration(validity)            // setExpiration -> expiration
                .signWith(key)                   // 알고리즘 자동 감지
                .compact();
    }

    public String getEmail(String token) {
        return Jwts.parser()                     // parserBuilder() -> parser()
                .verifyWith(key)                 // setSigningKey() -> verifyWith()
                .build()
                .parseSignedClaims(token)        // parseClaimsJws() -> parseSignedClaims()
                .getPayload()                    // getBody() -> getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}