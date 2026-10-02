package com.example.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

/**
 * Sem Mockito e sem Spring: criamos o JwtService "na mão" com new.
 * Como os campos @Value são privados, preenchemos com ReflectionTestUtils.
 */
class JwtServiceTest {

    private static final String SECRET =
            "NDNmZDQ1M2E0OTMwODZhMWQwNjQ1NmQ0NmZmMzY3ZDY4MzVjMTIzNGY2NzhhYmNkMTIzNDU2Nzg5MGFiY2RlZg==";

    private JwtService jwtService;
    private UserDetails admin;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", 60_000L);   // 1 minuto
        admin = User.builder().username("admin").password("x").roles("ADMIN").build();
    }

    @Test
    void deveGerarTokenENaoVazio() {
        String token = jwtService.gerarToken(admin);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);   // um JWT tem 3 partes: header.payload.assinatura
    }

    @Test
    void deveExtrairOUsernameDoToken() {
        String token = jwtService.gerarToken(admin);

        assertThat(jwtService.extrairUsername(token)).isEqualTo("admin");
    }

    @Test
    void deveConsiderarValidoUmTokenDoMesmoUsuario() {
        String token = jwtService.gerarToken(admin);

        assertThat(jwtService.tokenValido(token, admin)).isTrue();
    }

    @Test
    void naoDeveSerValidoParaOutroUsuario() {
        String token = jwtService.gerarToken(admin);
        UserDetails outro = User.builder().username("outro").password("x").roles("ADMIN").build();

        assertThat(jwtService.tokenValido(token, outro)).isFalse();
    }

    @Test
    void deveRejeitarTokenExpirado() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1000L);   // já nasce vencido
        String token = jwtService.gerarToken(admin);

        // A biblioteca JWT lança exceção ao ler um token vencido
        assertThatThrownBy(() -> jwtService.tokenValido(token, admin))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void deveRejeitarTokenAdulterado() {
        String token = jwtService.gerarToken(admin);
        String adulterado = token.substring(0, token.length() - 3) + "abc";   // estraga a assinatura

        assertThatThrownBy(() -> jwtService.extrairUsername(adulterado))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void deveRejeitarTextoQueNaoEUmToken() {
        assertThatThrownBy(() -> jwtService.extrairUsername("lixo"))
                .isInstanceOf(JwtException.class);
    }
}