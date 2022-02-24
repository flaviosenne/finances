package com.project.finances.infra.service.jwt;

import com.project.finances.domain.protocols.TokenProtocol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtTokenService implements TokenProtocol {

    @Value("${security.jwt.token.secret-key}")
    private final String secretKey;

    @Value("${security.jwt.token.expire-length}")
    private final String expireIn;

    @Override
    public String generateToken(String id){
        Claims claims = Jwts.claims().setSubject(id);

        Date today = new Date();
        Date validate = new Date(today.getTime() + Long.parseLong(this.expireIn));

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(today)
                .setExpiration(validate)
                .signWith(SignatureAlgorithm.HS512, this.secretKey)
                .compact();
    }

    @Override
    public String decodeToken(String token){
        return Jwts.parser()
                .setSigningKey(this.secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String resolveToken(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");

        return bearerToken != null && bearerToken.startsWith("Bearer ") ?
                bearerToken.substring(7): null;
    }

    @Override
    public boolean tokenIsValid(String token){
        try{
            Jwts.parser().setSigningKey(this.secretKey).parseClaimsJws(token);
            return true;
        }catch (JwtException | IllegalArgumentException e){
            return false;
        }
    }
}
