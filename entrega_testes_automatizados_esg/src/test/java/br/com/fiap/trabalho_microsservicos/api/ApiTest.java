package br.com.fiap.trabalho_microsservicos.api;

import br.com.fiap.trabalho_microsservicos.model.Equipamento;
import br.com.fiap.trabalho_microsservicos.model.Usuario;
import br.com.fiap.trabalho_microsservicos.repository.EquipamentoRepository;
import br.com.fiap.trabalho_microsservicos.repository.UsuarioRepository;
import br.com.fiap.trabalho_microsservicos.service.TokenService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EquipamentoRepository equipamentoRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String validToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        equipamentoRepository.deleteAll();

        if (usuarioRepository.findByLogin("admin@fiap.com.br") == null) {
            Usuario usuario = new Usuario();
            usuario.setLogin("admin@fiap.com.br");
            usuario.setSenha(passwordEncoder.encode("123456"));
            usuarioRepository.save(usuario);
        }

        Usuario usuario = (Usuario) usuarioRepository.findByLogin("admin@fiap.com.br");
        validToken = "Bearer " + tokenService.gerarToken(usuario);
    }

    @Test
    @DisplayName("POST /api/auth/login - Sucesso na autenticacao e validacao de contrato")
    void deveAutenticarComSucessoEValidarContrato() {
        String payload = "{\"login\": \"admin@fiap.com.br\", \"senha\": \"123456\"}";

        given()
                .contentType(ContentType.JSON)
                .body(payload)
        .when()
                .post("/api/auth/login")
        .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body(matchesJsonSchemaInClasspath("schemas/login-response-schema.json"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Falha na autenticacao com credenciais invalidas")
    void deveRetornar401ParaCredenciaisInvalidas() {
        String payload = "{\"login\": \"admin@fiap.com.br\", \"senha\": \"senha_incorreta\"}";

        given()
                .contentType(ContentType.JSON)
                .body(payload)
        .when()
                .post("/api/auth/login")
        .then()
                .statusCode(401)
                .body(containsString("Falha na autenticação"));
    }

    @Test
    @DisplayName("POST /api/energia/equipamentos - Cadastrar equipamento com sucesso e validar contrato")
    void deveCadastrarEquipamentoEValidarContrato() {
        String payload = "{\"nome\": \"Painel Solar\", \"setor\": \"Energia Limpa\", \"limiteConsumoKwh\": 500.0, \"status\": \"ATIVO\"}";

        given()
                .header("Authorization", validToken)
                .contentType(ContentType.JSON)
                .body(payload)
        .when()
                .post("/api/energia/equipamentos")
        .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("nome", equalTo("Painel Solar"))
                .body("setor", equalTo("Energia Limpa"))
                .body("limiteConsumoKwh", equalTo(500.0F))
                .body("status", equalTo("ATIVO"))
                .body(matchesJsonSchemaInClasspath("schemas/equipamento-schema.json"));
    }

    @Test
    @DisplayName("POST /api/energia/equipamentos - Falha de validacao com campos invalidos")
    void deveRetornar400ParaCadastroComDadosInvalidos() {
        String payload = "{\"nome\": \"\", \"setor\": \"\", \"limiteConsumoKwh\": -10.0, \"status\": \"ATIVO\"}";

        given()
                .header("Authorization", validToken)
                .contentType(ContentType.JSON)
                .body(payload)
        .when()
                .post("/api/energia/equipamentos")
        .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("GET /api/energia/equipamentos - Listar equipamentos e validar contrato JSON Schema")
    void deveListarEquipamentosEValidarContrato() {
        Equipamento eq = new Equipamento(null, "Turbina Eolica", "Geracao", 800.0, "ATIVO");
        equipamentoRepository.save(eq);

        given()
                .header("Authorization", validToken)
        .when()
                .get("/api/energia/equipamentos")
        .then()
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(1)))
                .body("[0].nome", equalTo("Turbina Eolica"))
                .body(matchesJsonSchemaInClasspath("schemas/equipamentos-list-schema.json"));
    }

    @Test
    @DisplayName("GET /api/energia/alertas - Listar equipamentos em alerta e validar contrato")
    void deveListarAlertasEValidarContrato() {
        Equipamento alerta = new Equipamento(null, "Compressor Central", "Fabrica", 300.0, "ALERTA");
        equipamentoRepository.save(alerta);

        given()
                .header("Authorization", validToken)
        .when()
                .get("/api/energia/alertas")
        .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].status", equalTo("ALERTA"))
                .body(matchesJsonSchemaInClasspath("schemas/equipamentos-list-schema.json"));
    }

    @Test
    @DisplayName("PUT /api/energia/equipamentos/{id}/desligar - Desligar equipamento via IoT com sucesso")
    void deveDesligarEquipamentoComSucesso() {
        Equipamento eq = new Equipamento(null, "Iluminacao Patio", "Externo", 100.0, "ATIVO");
        Equipamento salvo = equipamentoRepository.save(eq);

        given()
                .header("Authorization", validToken)
        .when()
                .put("/api/energia/equipamentos/" + salvo.getId() + "/desligar")
        .then()
                .statusCode(200)
                .body(equalTo("Equipamento " + salvo.getId() + " desligado com sucesso via IoT."));
    }

    @Test
    @DisplayName("PUT /api/energia/equipamentos/{id}/desligar - Retornar 404 para ID inexistente")
    void deveRetornar404AoDesligarEquipamentoInexistente() {
        given()
                .header("Authorization", validToken)
        .when()
                .put("/api/energia/equipamentos/999999/desligar")
        .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("GET /api/energia/estatisticas - Obter metricas de sustentabilidade ESG e validar contrato")
    void deveObterEstatisticasEValidarContrato() {
        equipamentoRepository.save(new Equipamento(null, "Servidor 1", "TI", 250.0, "ATIVO"));
        equipamentoRepository.save(new Equipamento(null, "Servidor 2", "TI", 250.0, "OCIOSO"));
        equipamentoRepository.save(new Equipamento(null, "Servidor 3", "TI", 250.0, "DESLIGADO"));

        given()
                .header("Authorization", validToken)
        .when()
                .get("/api/energia/estatisticas")
        .then()
                .statusCode(200)
                .body("ativos", equalTo(1))
                .body("ociosos", equalTo(1))
                .body("desligados", equalTo(1))
                .body(matchesJsonSchemaInClasspath("schemas/estatisticas-schema.json"));
    }

    @Test
    @DisplayName("Segurança - Retornar 403 Forbidden para requisicao sem token")
    void deveRetornar403ParaRequisicaoSemToken() {
        given()
        .when()
                .get("/api/energia/equipamentos")
        .then()
                .statusCode(403);
    }
}
