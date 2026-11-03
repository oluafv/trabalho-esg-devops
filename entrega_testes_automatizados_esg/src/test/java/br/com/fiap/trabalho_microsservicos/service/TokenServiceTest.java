package br.com.fiap.trabalho_microsservicos.service;

import br.com.fiap.trabalho_microsservicos.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", "segredo-teste-fiap-esg-123456");
    }

    @Test
    @DisplayName("Deve gerar token JWT válido com login do usuário no subject")
    void deveGerarTokenValido() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setLogin("admin@fiap.com.br");
        usuario.setSenha("123456");

        String token = tokenService.gerarToken(usuario);

        assertNotNull(token);
        assertFalse(token.isBlank());

        String subject = tokenService.getSubject(token);
        assertEquals("admin@fiap.com.br", subject);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar validar token inválido")
    void deveLancarExcecaoParaTokenInvalido() {
        assertThrows(RuntimeException.class, () -> {
            tokenService.getSubject("token.invalido.falso");
        });
    }
}
