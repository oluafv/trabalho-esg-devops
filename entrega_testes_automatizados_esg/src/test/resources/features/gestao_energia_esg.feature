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
