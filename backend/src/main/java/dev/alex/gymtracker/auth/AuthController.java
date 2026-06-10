package dev.alex.gymtracker.auth;

import dev.alex.gymtracker.common.ErrorResponse;
import dev.alex.gymtracker.config.AppProperties;
import dev.alex.gymtracker.config.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppProperties props;
    private final JwtService jwtService;

    public AuthController(AppProperties props, JwtService jwtService) {
        this.props = props;
        this.jwtService = jwtService;
    }

    @PostMapping("/token")
    public ResponseEntity<?> issueToken(@RequestBody AuthRequest request) {
        if (!props.username().equals(request.username())
                || !BCrypt.checkpw(request.password(), props.passwordHash())) {
            return ResponseEntity.status(401).body(
                new ErrorResponse(401, "Unauthorized", "Invalid credentials", Instant.now(), null));
        }
        String token = jwtService.generateToken(request.username());
        return ResponseEntity.ok(new AuthResponse(token, "Bearer", props.jwtTtlSeconds()));
    }

    public record AuthRequest(String username, String password) {}
    public record AuthResponse(String access_token, String token_type, long expires_in) {}
}
