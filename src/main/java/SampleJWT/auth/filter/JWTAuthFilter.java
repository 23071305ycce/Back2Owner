package SampleJWT.auth.filter;

import SampleJWT.auth.dto.LoginDTO;
import SampleJWT.auth.util.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JWTAuthFilter extends OncePerRequestFilter {
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    public JWTAuthFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if(!request.getServletPath().equals("/generate-token")){
            filterChain.doFilter(request,response);
            return ;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        LoginDTO loginDTO = objectMapper.readValue(request.getInputStream(), LoginDTO.class);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(loginDTO.getUsername(),loginDTO.getPassword());

        Authentication authResult = authenticationManager.authenticate(authToken);

        if(authResult.isAuthenticated()){
            String token = jwtUtil.generateToken(authResult.getName(), 15);
            response.setHeader("Authorization", "Bearer" + token);
        }

    }
}
