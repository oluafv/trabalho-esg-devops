# Documentacao Tecnica: Testes Automatizados e Validacao ESG
## Fase: Testes Automatizados - FIAP

---

### Identificacao do Projeto
- **Titulo do Projeto:** Testes Automatizados, BDD e Validacao de Contrato em Microsservico ESG de Monitoramento Energetico
- **Curso:** Engenharia / Analise e Desenvolvimento de Sistemas - FIAP
- **Disciplina:** Testes Automatizados e Qualidade de Software
- **Integrantes:**
  - *[Nome do Aluno / Integrantes do Grupo]* - *[RM]*

---

## 1. Integracao dos Pilares ESG nos Testes de Software

Os testes automatizados implementados asseguram a confiabilidade da aplicacao dentro dos pilares ESG (*Environmental, Social and Governance*):

1. **Eficiencia Energetica e Sustentabilidade (Ambiental - E):**
   - Testes automatizados validam a atribuicao de limites estritos de consumo em kWh por setor urbano/predial.
   - Cenarios BDD garantem que, ao registrar sobreconsumo, o sistema gere alertas imediatos e permita a atuacao automatizada via IoT para desligamento de equipamentos em sobrecarga, reduzindo o desperdicio eletrico.

2. **Governanca, Compliance e Integridade (Governanca - G):**
   - Testes de seguranca garantem que todas as operacoes sobre os recursos energeticos sejam autenticadas via JSON Web Token (JWT) com assinatura HMAC-SHA256 e senhas em hash BCrypt.
   - Testes de contrato (JSON Schema) asseguram conformidade e rastreabilidade dos dados trafegados entre os microsservicos e sensores IoT, impedindo que campos inconsistentes corrompam a base historica de consumo.

3. **Confiabilidade e Resiliencia (Social / Qualidade):**
   - Validacao de cenarios negativos (requisicoes invalidas, recursos inexistentes, credenciais invalidas) garantindo que o sistema responda com os devidos codigos HTTP (400, 401, 403, 404) e mensagens claras, sem expor dados internos ou entrar em estado instavel.

---

## 2. Descricao dos Cenarios de Teste BDD (Gherkin)

A implementacao utiliza **Cucumber-JVM** e **Spring Boot Test**, integrando o arquivo de especificacao executavel `src/test/resources/features/gestao_energia_esg.feature`.

### Cenario 1: Cadastrar novo equipamento de monitoramento energetico com sucesso (Caminho Feliz)
- **Objetivo:** Garantir que um equipamento com dados validos seja persistido e receba o status operacional inicial "ATIVO".
- **Gherkin:**
  ```gherkin
  Scenario: Cadastrar novo equipamento de monitoramento energetico com sucesso
    Given que o operador esta autenticado no sistema
    When enviar a requisicao de cadastro de um equipamento valido para o setor "Sustentabilidade" com limite 120.0 kWh
    Then o sistema deve cadastrar o equipamento retornando status 201
    And o status inicial do equipamento deve ser "ATIVO"
  ```
- **Resultado Esperado:** Codigo HTTP 201 Created e corpo JSON com `status: "ATIVO"`.

### Cenario 2: Rejeitar cadastro de equipamento com dados invalidos (Caminho de Falha / Validacao)
- **Objetivo:** Assegurar que equipamentos com nome vazio ou limite de consumo negativo sejam barrados pela camada de validacao Bean Validation.
- **Gherkin:**
  ```gherkin
  Scenario: Rejeitar cadastro de equipamento com dados invalidos
    Given que o operador esta autenticado no sistema
    When enviar a requisicao de cadastro de um equipamento com nome vazio e limite negativo
    Then o sistema deve recusar o cadastro retornando status 400
  ```
- **Resultado Esperado:** Codigo HTTP 400 Bad Request.

### Cenario 3: Desligamento remoto para contencao de consumo via IoT (Caminho Feliz ESG)
- **Objetivo:** Validar o comando IoT de atuacao remota que comuta o estado do equipamento para "DESLIGADO", estancando o desperdicio de energia.
- **Gherkin:**
  ```gherkin
  Scenario: Desligar remotamente equipamento para contencao de consumo via IoT
    Given que o operador esta autenticado no sistema
    And existe um equipamento cadastrado com status "ATIVO"
    When o operador solicitar o desligamento remoto do equipamento
    Then o sistema deve confirmar o desligamento retornando status 200
    And o status do equipamento deve ser atualizado para "DESLIGADO"
  ```
- **Resultado Esperado:** Codigo HTTP 200 OK e persistencia com status "DESLIGADO".

### Cenario 4: Rejeitar desligamento de equipamento inexistente (Caminho de Falha)
- **Objetivo:** Validar a resposta do sistema ao receber comando de desligamento para um identificador que nao existe na base de dados.
- **Gherkin:**
  ```gherkin
  Scenario: Rejeitar desligamento de equipamento inexistente
    Given que o operador esta autenticado no sistema
    When o operador solicitar o desligamento de um equipamento inexistente com id 999999
    Then o sistema deve responder com status 404
  ```
- **Resultado Esperado:** Codigo HTTP 404 Not Found.

### Cenario 5 e 6: Autenticacao de usuarios (Esquema do Cenario - Positivo e Negativo)
- **Objetivo:** Validar autenticacao com credenciais validas e rejeicao com status 401 para credenciais incorretas.
- **Gherkin:**
  ```gherkin
  Scenario Outline: Autenticacao de usuarios no sistema
    When o usuario submeter o login com "<login>" e senha "<senha>"
    Then a resposta da autenticacao deve ter o status <status_code>

    Examples:
      | login             | senha          | status_code |
      | admin@fiap.com.br | 123456         | 200         |
      | admin@fiap.com.br | senha_invalida | 401         |
  ```

---

## 3. Testes de API e Contrato com JSON Schema

A classe `ApiTest.java` implementa 10 testes com a biblioteca **REST Assured**, validando a comunicacao HTTP completa contra o servidor embedded:

1. **`POST /api/auth/login` (Sucesso):** Status 200, presenca do token JWT e contrato validado contra `login-response-schema.json`.
2. **`POST /api/auth/login` (Credenciais Invalidas):** Status 401 e mensagem de erro explicita.
3. **`POST /api/energia/equipamentos` (Sucesso):** Status 201, validacao de atributos persistidos e contrato validado contra `equipamento-schema.json`.
4. **`POST /api/energia/equipamentos` (Invalido):** Status 400 ao enviar campos em branco ou limites negativos.
5. **`GET /api/energia/equipamentos`:** Status 200, array JSON populado e contrato validado contra `equipamentos-list-schema.json`.
6. **`GET /api/energia/alertas`:** Status 200, filtragem de equipamentos em situacao de alerta de sobreconsumo e contrato contra `equipamentos-list-schema.json`.
7. **`PUT /api/energia/equipamentos/{id}/desligar` (Sucesso):** Status 200 e mensagem de confirmacao da atuacao IoT.
8. **`PUT /api/energia/equipamentos/{id}/desligar` (Nao Encontrado):** Status 404 para ID inexistente.
9. **`GET /api/energia/estatisticas`:** Status 200, campos numricos `ativos`, `ociosos` e `desligados` validados contra `estatisticas-schema.json`.
10. **Seguranca HTTP:** Status 403 Forbidden para chamadas sem header `Authorization: Bearer <token>`.

---

## 4. Evidencias de Execucao dos Testes

Ao executar `./mvnw clean test`, a suite roda 25 testes com 100% de sucesso:

```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running br.com.fiap.trabalho_microsservicos.api.ApiTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running br.com.fiap.trabalho_microsservicos.bdd.CucumberTestRunner
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running br.com.fiap.trabalho_microsservicos.service.EquipamentoServiceTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running br.com.fiap.trabalho_microsservicos.service.TokenServiceTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running br.com.fiap.trabalho_microsservicos.TrabalhoMicrosservicosApplicationTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 25, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## 5. Roteiro de Captura de Prints para PDF / PPT

Para compor a entrega final em PDF ou apresentacao em slides:

1. **Print 1: Execucao Geral no Terminal (`./mvnw clean test`)**
   - Mostre o terminal com a mensagem `BUILD SUCCESS` e o total de 25 testes executados sem falhas.

2. **Print 2: Execucao dos Cenarios BDD com Cucumber (`./mvnw test -Dtest=CucumberTestRunner`)**
   - Mostre a execucao dos 6 cenarios BDD passando com sucesso.

3. **Print 3: Relatorio HTML do Cucumber**
   - Abra no navegador o arquivo `target/cucumber-reports/cucumber.html` e capture a tela exibindo os cenarios verdes.

4. **Print 4: Execucao dos Testes de API e Contrato (`./mvnw test -Dtest=ApiTest`)**
   - Mostre os 10 testes de API com validacao de JSON Schema passando.

5. **Print 5: Pipeline CI/CD no GitHub Actions**
   - Capture a execucao da esteira no GitHub Actions mostrando o job `1. Build e Testes Automatizados` com os artefatos de relatorio publicados.

---

## 6. Checklist de Entrega

| Item | Status |
|---|:---:|
| Projeto compactado em .ZIP com estrutura organizada | Concluido |
| No minimo 3 cenarios de teste BDD em Gherkin (positivos e negativos) | Concluido (6 cenarios) |
| Testes de API para todas as rotas do projeto | Concluido (10 testes) |
| Validacao de status code e corpo de resposta JSON | Concluido |
| Testes de contrato com JSON Schema | Concluido (4 schemas) |
| README com instrucoes de configuracao e execucao | Concluido |
| Documentacao tecnica com evidencias e descricao Gherkin | Concluido |
