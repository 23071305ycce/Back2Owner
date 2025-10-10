package SampleJWT.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JWTUtil {
    private static final String SECRET_KEY =
            "fasfkskfjsdjfksflkjfjdsfjsdfjsdkfjskfjksdfjjfdjfsjfksdjfkjsdnc"; // >= 32 bytes for HS256
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

    // Extract username (sub)
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Validate token (signature + expiry + subject match)
    public boolean validateToken(String token, String expectedUsername) {
        try {
            final String username = extractUsername(token);
            return username != null && username.equals(expectedUsername) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    // Check expiry
    public boolean isTokenExpired(String token) {
        Date exp = extractClaim(token, Claims::getExpiration);
        return exp.before(new Date());
    }

    // Generic claim extractor
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Parse and return all claims
    private Claims extractAllClaims(String token) {
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
        return jws.getBody();
    }
}
