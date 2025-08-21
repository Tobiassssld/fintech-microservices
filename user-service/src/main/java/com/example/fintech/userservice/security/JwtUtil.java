package com.example.fintech.userservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.hibernate.boot.model.source.spi.SingularAttributeNature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret:mySecretKeyForFintech2025DevelopmentOnly12345678901234567890}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    private SecretKey getSigningKey(){
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    //Generating JWT Token

    public String generateToken(String username){
        return createToken(username);
    }

    private String createToken(String subject) {
        long currentTime = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(currentTime))
                .setExpiration(new Date(currentTime + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    //extract username from token
    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    //extract expiredtime from token
    public Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    //check if the token is expired
    private Boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    //validate the token
    public Boolean validateToken(String token,String username){
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }


}
