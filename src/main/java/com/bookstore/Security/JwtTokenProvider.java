package com.bookstore.Security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
@Log4j2
public class JwtTokenProvider {
    private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    private final String issuer = "book-store";

    private Key getSigningKey() {
        return secretKey;
    }

    public String generateAccessToken(UserDetail userDetail) {
        Date now = new Date();
        long JWT_ACCESS_EXPIRATION = 60 * 60 * 1000L;
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
        long JWT_REFRESH_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;
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

    public boolean _validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException ex) {
            System.out.println("Expired token : " + ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            System.out.println("Token not supported : " + ex.getMessage());
        } catch (MalformedJwtException ex) {
            System.out.println("Incorrect format token : " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            System.out.println("Token is empty or null : " + ex.getMessage());
        }
        return false;
    }


    public boolean validateToken(String authToken) throws JwtException {
        Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(authToken);
        return true;
    }
}
