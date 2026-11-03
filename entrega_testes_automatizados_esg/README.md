# Projeto - Cidades ESG Inteligentes: Testes Automatizados

Sistema de monitoramento e gestao de eficiencia energetica para cidades inteligentes (ESG) com suíte completa de **Testes Automatizados**: **BDD com Cucumber e Gherkin**, **Testes de API com REST Assured**, **Testes de Contrato com JSON Schema** e esteira **CI/CD no GitHub Actions**.

---

## 1. Visão Geral da Suíte de Testes

A suíte de testes valida os pilares tecnicos e os requisitos de sustentabilidade (ESG) do sistema:
- **BDD (Behavior Driven Development):** 6 cenarios em linguagem Gherkin validando caminhos felizes e de excecao (rejeicao de dados invalidos, atuacao remota IoT para contencao de sobreconsumo e autenticacao de usuarios).
- **Testes de API e Contrato:** 10 testes cobrindo todos os endpoints da aplicacao com validacao de Status Code, corpo de resposta JSON e conformidade estrita de contrato via JSON Schema (Draft-07).
- **Testes Unitarios e Integracao:** 9 testes cobrindo regras de negocio de medicao energetica, alertas, calculo de estatisticas ESG e geracao/validacao de tokens JWT.
- **Total:** 25 testes automatizados executados de forma isolada com banco H2 em memoria.

---

## 2. Estrutura de Testes no Projeto

```
src/test/
├── java/br/com/fiap/trabalho_microsservicos/
│   ├── api/
│   │   └── ApiTest.java                     # Testes de API, Status Codes, JSON e Contratos JSON Schema
│   ├── bdd/
│   │   ├── CucumberSpringConfiguration.java # Contexto Spring Boot para execucao dos cenarios BDD
│   │   ├── CucumberTestRunner.java          # Suite Runner do Cucumber com JUnit Platform
│   │   └── GestaoEnergiaStepDefinitions.java# Implementacao dos passos Gherkin (Given, When, Then, And)
│   ├── service/
│   │   ├── EquipamentoServiceTest.java      # Testes unitarios da logica de negocio ESG (Mockito)
│   │   └── TokenServiceTest.java            # Testes unitarios de autenticacao JWT
│   └── TrabalhoMicrosservicosApplicationTests.java # Teste de carregamento do contexto Spring
└── resources/
    ├── application.properties               # Configuracao isolada de testes com banco H2 em memoria
    ├── features/
    │   └── gestao_energia_esg.feature       # Cenarios BDD escritos em Gherkin
    └── schemas/
        ├── equipamento-schema.json          # Contrato JSON Schema do Equipamento
        ├── equipamentos-list-schema.json    # Contrato JSON Schema da Listagem de Equipamentos
        ├── estatisticas-schema.json         # Contrato JSON Schema das Metricas ESG
        └── login-response-schema.json       # Contrato JSON Schema da Autenticacao JWT
```

---

## 3. Cenários BDD em Gherkin

Arquivo: `src/test/resources/features/gestao_energia_esg.feature`

```gherkin
Feature: Gestao e Monitoramento de Eficiencia Energetica ESG

  Scenario: Cadastrar novo equipamento de monitoramento energetico com sucesso
    Given que o operador esta autenticado no sistema
    When enviar a requisicao de cadastro de um equipamento valido para o setor "Sustentabilidade" com limite 120.0 kWh
    Then o sistema deve cadastrar o equipamento retornando status 201
    And o status inicial do equipamento deve ser "ATIVO"

  Scenario: Rejeitar cadastro de equipamento com dados invalidos
    Given que o operador esta autenticado no sistema
    When enviar a requisicao de cadastro de um equipamento com nome vazio e limite negativo
    Then o sistema deve recusar o cadastro retornando status 400

  Scenario: Desligar remotamente equipamento para contencao de consumo via IoT
    Given que o operador esta autenticado no sistema
    And existe um equipamento cadastrado com status "ATIVO"
    When o operador solicitar o desligamento remoto do equipamento
    Then o sistema deve confirmar o desligamento retornando status 200
    And o status do equipamento deve ser atualizado para "DESLIGADO"

  Scenario: Rejeitar desligamento de equipamento inexistente
    Given que o operador esta autenticado no sistema
    When o operador solicitar o desligamento de um equipamento inexistente com id 999999
    Then o sistema deve responder com status 404

  Scenario Outline: Autenticacao de usuarios no sistema
    When o usuario submeter o login com "<login>" e senha "<senha>"
    Then a resposta da autenticacao deve ter o status <status_code>

    Examples:
      | login             | senha          | status_code |
      | admin@fiap.com.br | 123456         | 200         |
      | admin@fiap.com.br | senha_invalida | 401         |
```

---

## 4. Cobertura de Testes das APIs e Contratos JSON Schema

| Endpoint | Metodo | Validacao de Status | Validacao do Corpo (JSON) | Validacao de Contrato (JSON Schema) |
|---|---|---|---|---|
| `/api/auth/login` | POST | 200 OK / 401 Unauthorized | Valida presenca e emissao do token JWT | `login-response-schema.json` |
| `/api/energia/equipamentos` | POST | 201 Created / 400 Bad Request | Valida persistencia dos atributos e status inicial ATIVO | `equipamento-schema.json` |
| `/api/energia/equipamentos` | GET | 200 OK | Valida retorno da lista e integridade dos itens | `equipamentos-list-schema.json` |
| `/api/energia/alertas` | GET | 200 OK | Valida filtragem por equipamentos com status ALERTA | `equipamentos-list-schema.json` |
| `/api/energia/equipamentos/{id}/desligar` | PUT | 200 OK / 404 Not Found | Valida atuacao remota IoT e mensagem de confirmacao | Resposta textual e atualizacao no banco |
| `/api/energia/estatisticas` | GET | 200 OK | Valida contadores de ativos, ociosos e desligados | `estatisticas-schema.json` |
| Endpoints Protegidos | Qualquer | 403 Forbidden | Valida bloqueio de acesso sem token de autenticacao | Protecao de camada HTTP Spring Security |

---

## 5. Como Executar os Testes Localmente

### Pre-requisitos
- JDK 21 instalado
- Git instalado

### Comandos de Execucao

1. **Executar toda a suite de testes (25 testes):**
   ```bash
   ./mvnw clean test
   ```
   No Windows PowerShell:
   ```powershell
   .\mvnw.cmd clean test
   ```

2. **Executar somente os testes BDD com Cucumber:**
   ```bash
   ./mvnw test -Dtest=CucumberTestRunner
   ```

3. **Executar somente os testes de API e Contrato:**
   ```bash
   ./mvnw test -Dtest=ApiTest
   ```

4. **Visualizar Relatorio de Testes BDD gerado:**
   Apos a execucao, o relatorio HTML do Cucumber e gerado em:
   `target/cucumber-reports/cucumber.html`

---

## 6. Execução em Pipeline CI/CD (GitHub Actions)

A esteira automatizada (`.github/workflows/ci-cd.yml`) executa os testes a cada push e pull request:
1. `Checkout` do codigo fonte.
2. Setup do JDK 21 Temurin com cache inteligente Maven.
3. Compilacao do projeto (`./mvnw clean compile`).
4. Execucao de todos os testes unitarios, BDD e de API (`./mvnw test`).
5. Publicacao dos relatorios do Surefire e Cucumber como artefatos de build (`upload-artifact@v4`).
6. Build e publicacao da imagem Docker e deploy automatizado em Staging e Producao.

---

## 7. Checklist da Atividade

| Item | Status |
|---|:---:|
| No minimo 3 cenarios de teste BDD em Gherkin (positivos e negativos) | Concluido (6 cenarios) |
| Testes de API para todos os endpoints do projeto | Concluido (10 testes) |
| Validacao de status code em todas as rotas | Concluido |
| Validacao do corpo de resposta JSON | Concluido |
| Testes de contrato utilizando JSON Schema | Concluido (4 schemas) |
| Execucao local e em pipeline de CI/CD | Concluido e documentado |
| Codigo-fonte compactado em .ZIP | Concluido |
| Documentacao tecnica com roteiro de evidencias | Concluido |
