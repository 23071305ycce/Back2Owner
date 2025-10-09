package SampleJWT.auth.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JWTUtil {
    private static final String SECRET_KEY =
            "fasfkskfjsdjfksflkjfjdsfjsdfjsdkfjskfjksdfjjfdjfsjfksdjfkjsdnc";
    private static final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

    // Generate JWT token
    public String generateToken(String username, long expiryMinutes) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiryMinutes * 60 * 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
