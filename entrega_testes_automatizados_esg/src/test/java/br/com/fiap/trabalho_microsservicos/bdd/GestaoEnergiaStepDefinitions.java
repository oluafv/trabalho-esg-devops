package br.com.fiap.trabalho_microsservicos.bdd;

import br.com.fiap.trabalho_microsservicos.model.Equipamento;
import br.com.fiap.trabalho_microsservicos.model.Usuario;
import br.com.fiap.trabalho_microsservicos.repository.EquipamentoRepository;
import br.com.fiap.trabalho_microsservicos.repository.UsuarioRepository;
import br.com.fiap.trabalho_microsservicos.service.TokenService;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GestaoEnergiaStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EquipamentoRepository equipamentoRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String authToken;
    private ResultActions resultActions;
    private Long equipamentoId;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() {
        if (usuarioRepository.findByLogin("admin@fiap.com.br") == null) {
            Usuario usuario = new Usuario();
            usuario.setLogin("admin@fiap.com.br");
            usuario.setSenha(passwordEncoder.encode("123456"));
            usuarioRepository.save(usuario);
        }
    }

    @Given("que o operador esta autenticado no sistema")
    public void queOOperadorEstaAutenticadoNoSistema() {
        Usuario usuario = (Usuario) usuarioRepository.findByLogin("admin@fiap.com.br");
        authToken = "Bearer " + tokenService.gerarToken(usuario);
    }

    @When("enviar a requisicao de cadastro de um equipamento valido para o setor {string} com limite {double} kWh")
    public void enviarARequisicaoDeCadastroDeUmEquipamentoValido(String setor, Double limite) throws Exception {
        Equipamento equipamento = new Equipamento(null, "Ar Condicionado Smart", setor, limite, "ATIVO");
        String payload = objectMapper.writeValueAsString(equipamento);

        resultActions = mockMvc.perform(post("/api/energia/equipamentos")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));
    }

    @Then("o sistema deve cadastrar o equipamento retornando status {int}")
    public void oSistemaDeveCadastrarOEquipamentoRetornandoStatus(int statusEsperado) throws Exception {
        resultActions.andExpect(status().is(statusEsperado));
    }

    @And("o status inicial do equipamento deve ser {string}")
    public void oStatusInicialDoEquipamentoDeveSer(String statusEsperado) throws Exception {
        resultActions.andExpect(jsonPath("$.status").value(statusEsperado));
    }

    @When("enviar a requisicao de cadastro de um equipamento com nome vazio e limite negativo")
    public void enviarARequisicaoDeCadastroComDadosInvalidos() throws Exception {
        String payload = "{\"nome\": \"\", \"setor\": \"RH\", \"limiteConsumoKwh\": -50.0, \"status\": \"ATIVO\"}";

        resultActions = mockMvc.perform(post("/api/energia/equipamentos")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));
    }

    @Then("o sistema deve recusar o cadastro retornando status {int}")
    public void oSistemaDeveRecusarOCadastroRetornandoStatus(int statusEsperado) throws Exception {
        resultActions.andExpect(status().is(statusEsperado));
    }

    @And("existe um equipamento cadastrado com status {string}")
    public void existeUmEquipamentoCadastradoComStatus(String status) {
        Equipamento equipamento = new Equipamento(null, "Bomba Hidraulica", "Infraestrutura", 200.0, status);
        Equipamento salvo = equipamentoRepository.save(equipamento);
        equipamentoId = salvo.getId();
    }

    @When("o operador solicitar o desligamento remoto do equipamento")
    public void oOperadorSolicitarODesligamentoRemotoDoEquipamento() throws Exception {
        resultActions = mockMvc.perform(put("/api/energia/equipamentos/" + equipamentoId + "/desligar")
                .header("Authorization", authToken));
    }

    @Then("o sistema deve confirmar o desligamento retornando status {int}")
    public void oSistemaDeveConfirmarODesligamentoRetornandoStatus(int statusEsperado) throws Exception {
        resultActions.andExpect(status().is(statusEsperado));
    }

    @And("o status do equipamento deve ser atualizado para {string}")
    public void oStatusDoEquipamentoDeveSerAtualizadoPara(String statusEsperado) {
        Equipamento atualizado = equipamentoRepository.findById(equipamentoId).orElseThrow();
        assertEquals(statusEsperado, atualizado.getStatus());
    }

    @When("o operador solicitar o desligamento de um equipamento inexistente com id {long}")
    public void oOperadorSolicitarODesligamentoDeEquipamentoInexistente(Long idInexistente) throws Exception {
        resultActions = mockMvc.perform(put("/api/energia/equipamentos/" + idInexistente + "/desligar")
                .header("Authorization", authToken));
    }

    @Then("o sistema deve responder com status {int}")
    public void oSistemaDeveResponderComStatus(int statusEsperado) throws Exception {
        resultActions.andExpect(status().is(statusEsperado));
    }

    @When("o usuario submeter o login com {string} e senha {string}")
    public void oUsuarioSubmeterOLoginComESenha(String login, String senha) throws Exception {
        String payload = String.format("{\"login\": \"%s\", \"senha\": \"%s\"}", login, senha);

        resultActions = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));
    }

    @Then("a resposta da autenticacao deve ter o status {int}")
    public void aRespostaDaAutenticacaoDeveTerOStatus(int statusEsperado) throws Exception {
        resultActions.andExpect(status().is(statusEsperado));
    }
}
