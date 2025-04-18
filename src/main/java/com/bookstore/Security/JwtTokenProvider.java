package com.bookstore.Security;

import com.bookstore.Repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
@Log4j2
public class JwtTokenProvider {

    @Autowired
    UserRepository userRepository;

    private final Long JWT_ACCESS_EXPIRATION = 60 * 60 * 1000L; // 60 milliseconds? Bạn nên tăng lên nếu cần thực tế hơn
    private final Long JWT_REFRESH_EXPIRATION = 7 * 24 * 60 * 60 * 1000L; // 7 ngày

    private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    private final String issuer = "canhnam357";

    private Key getSigningKey() {
        return secretKey;
    }

    public String generateAccessToken(UserDetail userDetail) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_ACCESS_EXPIRATION);
        return Jwts.builder()
                .setSubject(userDetail.getUser().getUserId())
                .claim("user_id", userDetail.getUser().getUserId())
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateRefreshToken(UserDetail userDetail) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_REFRESH_EXPIRATION);

        return Jwts.builder()
                .setSubject(userDetail.getUser().getUserId())
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUserIdFromJwt(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("user_id", String.class);
    }

    public String getUserIdFromRefreshToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String authToken) throws JwtException {
        // Không bắt lỗi ở đây, để JwtAuthenticationFilter xử lý
        Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(authToken);
        return true;
    }
}
