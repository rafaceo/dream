package com.demo.cards.service;

import com.demo.cards.config.JwtProperties;
import com.demo.cards.dto.LoginRequest;
import com.demo.cards.dto.LoginResponse;
import com.demo.cards.exception.InvalidCredentialsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

@Service
public class AuthService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;
    private final String username;
    private final String password;

    public AuthService(JwtEncoder jwtEncoder, JwtProperties jwtProperties,
                       @Value("${app.auth.username}") String username,
                       @Value("${app.auth.password}") String password) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
        this.username = username;
        this.password = password;
    }

    public LoginResponse login(LoginRequest request) {
        // both checks always run and compare in constant time
        boolean usernameOk = constantTimeEquals(username, request.username());
        boolean passwordOk = constantTimeEquals(password, request.password());
        if (!(usernameOk & passwordOk)) {
            throw new InvalidCredentialsException();
        }
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(request.username())
                .issuedAt(now)
                .expiresAt(now.plus(jwtProperties.ttl()))
                .build();
        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new LoginResponse(token, "Bearer", jwtProperties.ttl().toSeconds());
    }

    private static boolean constantTimeEquals(String expected, String actual) {
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }
}
