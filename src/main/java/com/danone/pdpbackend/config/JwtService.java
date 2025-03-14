package com.danone.pdpbackend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {

    private final String SECRET_KEY = "FegQKytiexcMSGV2QBHUObuI50isvTad";

    public String extractUsernameFromToken(String token) {
        return extractClaimFromToken(token, Claims::getSubject);
    }


    public String generateToken(UserDetails userDetails){
        return generateToken(new HashMap<>(), userDetails);
    }


    public String generateToken(Map<String,Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder().claims(extraClaims).subject(userDetails.getUsername()).issuedAt(new java.util.Date(System.currentTimeMillis()))
                .expiration(new java.util.Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public <T> T extractClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }


    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] secretBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public Boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public boolean isTokenExpired(String token) {
        try{
            final Date expiration = extractExpirationDateFromToken(token);
            return expiration.before(new Date());
        }catch (ExpiredJwtException expiredJwtException){
            return true;
        }

    }

    private Date extractExpirationDateFromToken(String token) {
        return extractClaimFromToken(token, Claims::getExpiration);
    }


}
