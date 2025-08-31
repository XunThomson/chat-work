package com.xun.Jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTool_1 {
    private String type = "JWT";
    private SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
    private SecretKey secretKey;
    private Long duration = 1000L * 60 * 60 * 24 * 30; // 默认有效期为30天

    public JwtTool_1() {
        this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    public JwtTool_1 setSignatureAlgorithm(SignatureAlgorithm signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
        return this;
    }

    public JwtTool_1 setDuration(Long duration) {
        this.duration = duration;
        return this;
    }

    public String getJwt(Map<String, Object> data) {
        return Jwts.builder()
                .setHeaderParam("typ", type)
                .setHeaderParam("alg", signatureAlgorithm.getValue())
                .signWith(secretKey, signatureAlgorithm)
                .setClaims(data)
                .setExpiration(new Date(System.currentTimeMillis() + duration))
                .compact();
    }

    private Claims parseClaims(String jwt) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();
        } catch (Exception e) {
            throw new RuntimeException("Error parsing JWT: " + e.getMessage(), e);
        }
    }

    public Claims analyzeJwt(String jwt) {
        if (jwt == null || jwt.isEmpty()) {
            throw new IllegalArgumentException("JWT string cannot be null or empty");
        }
        return parseClaims(jwt);
    }

    public Claims analyzeJwt(){
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();

        // 从 request 中获取拦截器中设置的 token
        Object tokenObj = request.getAttribute("authToken");
        String token = null;
        if (tokenObj instanceof String) {
            token = (String) tokenObj;
        } else {
            return null;
        }

        return this.analyzeJwt(token);
    }
}