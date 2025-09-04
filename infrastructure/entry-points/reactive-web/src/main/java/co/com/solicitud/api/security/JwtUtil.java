package co.com.solicitud.api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.List;


public class JwtUtil {
    private JwtUtil() {
    }

    public static String extractToken(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            return null;
        }
        if (authHeader.toLowerCase().startsWith("bearer")) {
            return authHeader.substring(6).trim();
        }
        return authHeader.trim();
    }

    private static DecodedJWT decode(String authHeader) {
        String token = extractToken(authHeader);
        return token != null ? JWT.decode(token) : null;
    }

    public static boolean isClient(String authHeader) {
        DecodedJWT jwt = decode(authHeader);
        if (jwt == null) {
            return false;
        }
        String role = jwt.getClaim("rol").asString();
        if (role == null) {
            role = jwt.getClaim("role").asString();
        }
        if ("3".equals(role)) {
            return true;
        }
        List<String> roles = jwt.getClaim("roles").asList(String.class);
        return roles != null && roles.contains("3");
    }
}
