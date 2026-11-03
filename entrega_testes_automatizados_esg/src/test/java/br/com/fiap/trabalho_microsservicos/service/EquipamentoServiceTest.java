package br.com.fiap.trabalho_microsservicos.service;

import br.com.fiap.trabalho_microsservicos.model.Equipamento;
import br.com.fiap.trabalho_microsservicos.repository.EquipamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipamentoServiceTest {

    @Mock
    private EquipamentoRepository repository;

    @InjectMocks
    private EquipamentoService service;

    private Equipamento equipamento;

    @BeforeEach
    void setUp() {
        equipamento = new Equipamento(1L, "Ar Condicionado", "RH", 150.0, "ATIVO");
    }

    @Test
    @DisplayName("Deve cadastrar equipamento definindo status inicial como ATIVO")
    void deveCadastrarEquipamento() {
        when(repository.save(any(Equipamento.class))).thenReturn(equipamento);

        Equipamento salvo = service.cadastrarEquipamento(new Equipamento(null, "Ar Condicionado", "RH", 150.0, null));

        assertNotNull(salvo);
        assertEquals("ATIVO", salvo.getStatus());
        verify(repository, times(1)).save(any(Equipamento.class));
    }

    @Test
    @DisplayName("Deve listar todos os equipamentos")
    void deveListarTodosEquipamentos() {
        when(repository.findAll()).thenReturn(List.of(equipamento));

        List<Equipamento> lista = service.listarTodos();

        assertFalse(lista.isEmpty());
        assertEquals(1, lista.size());
        assertEquals("Ar Condicionado", lista.get(0).getNome());
    }

    @Test
    @DisplayName("Deve desligar equipamento com sucesso")
    void deveDesligarEquipamentoComSucesso() {
        when(repository.findById(1L)).thenReturn(Optional.of(equipamento));
        when(repository.save(any(Equipamento.class))).thenReturn(equipamento);

        boolean desligado = service.desligarEquipamento(1L);

        assertTrue(desligado);
        assertEquals("DESLIGADO", equipamento.getStatus());
        verify(repository, times(1)).save(equipamento);
    }

    @Test
    @DisplayName("Deve gerar ALERTA quando medição exceder o limite de consumo")
    void deveGerarAlertaQuandoConsumoExcederLimite() {
        when(repository.findById(1L)).thenReturn(Optional.of(equipamento));
        when(repository.save(any(Equipamento.class))).thenReturn(equipamento);

        String resultado = service.registrarMedicao(1L, 200.0);

        assertTrue(resultado.contains("ALERTA"));
        assertEquals("ALERTA", equipamento.getStatus());
        verify(repository, times(1)).save(equipamento);
    }

    @Test
    @DisplayName("Deve registrar consumo normal quando medição estiver dentro do limite")
    void deveRegistrarConsumoNormalDentroDoLimite() {
        when(repository.findById(1L)).thenReturn(Optional.of(equipamento));

        String resultado = service.registrarMedicao(1L, 100.0);

        assertTrue(resultado.contains("Consumo normal"));
        assertEquals("ATIVO", equipamento.getStatus());
    }

    @Test
    @DisplayName("Deve contar corretamente a quantidade de equipamentos por status")
    void deveContarPorStatus() {
        Equipamento eq2 = new Equipamento(2L, "Lâmpadas LED", "TI", 50.0, "ATIVO");
        Equipamento eq3 = new Equipamento(3L, "Servidor", "TI", 300.0, "DESLIGADO");

        when(repository.findAll()).thenReturn(List.of(equipamento, eq2, eq3));

        long ativos = service.contarPorStatus("ATIVO");
        long desligados = service.contarPorStatus("DESLIGADO");

        assertEquals(2, ativos);
        assertEquals(1, desligados);
    }
}
