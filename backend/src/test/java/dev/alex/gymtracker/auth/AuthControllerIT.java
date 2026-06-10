package dev.alex.gymtracker.auth;

import dev.alex.gymtracker.common.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

class AuthControllerIT extends IntegrationTestBase {

    @Autowired
    TestRestTemplate rest;

    @Test
    void issueToken_validCredentials_returns200WithToken() {
        var request = new AuthController.AuthRequest("admin", "testpass");
        ResponseEntity<AuthController.AuthResponse> response =
            rest.postForEntity("/api/auth/token", request, AuthController.AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().access_token()).isNotBlank();
        assertThat(response.getBody().token_type()).isEqualTo("Bearer");
    }

    @Test
    void issueToken_wrongPassword_returns401() {
        var request = new AuthController.AuthRequest("admin", "wrong");
        ResponseEntity<Void> response =
            rest.postForEntity("/api/auth/token", request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void protectedEndpoint_withoutToken_returns401() {
        ResponseEntity<Void> response =
            rest.getForEntity("/api/exercises", Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
