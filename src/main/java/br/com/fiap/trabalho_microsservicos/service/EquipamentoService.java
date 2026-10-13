package br.com.fiap.trabalho_microsservicos.service;

import br.com.fiap.trabalho_microsservicos.model.Equipamento;
import br.com.fiap.trabalho_microsservicos.repository.EquipamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EquipamentoService {

    @Autowired
    private EquipamentoRepository repository;


    @Transactional
    public Equipamento cadastrarEquipamento(Equipamento equipamento) {
        equipamento.setStatus("ATIVO");
        return repository.save(equipamento);
    }

    public List<Equipamento> listarTodos() {
        return repository.findAll();
    }

    public Optional<Equipamento> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public List<Equipamento> listarAlertas() {
        return repository.findByStatus("ALERTA");
    }

    public long contarPorStatus(String status) {
        return repository.findAll().stream()
                .filter(e -> e.getStatus().equalsIgnoreCase(status))
                .count();
    }

    @Transactional
    public boolean desligarEquipamento(Long id) {
        Optional<Equipamento> equipamentoOpt = repository.findById(id);

        if (equipamentoOpt.isPresent()) {
            Equipamento equipamento = equipamentoOpt.get();
            equipamento.setStatus("DESLIGADO");
            repository.save(equipamento);
            return true;
        }
        return false;
    }


    @Transactional
    public String registrarMedicao(Long id, Double consumoAtual) {
        Optional<Equipamento> equipamentoOpt = repository.findById(id);

        if (equipamentoOpt.isPresent()) {
            Equipamento eq = equipamentoOpt.get();

            if (consumoAtual > eq.getLimiteConsumoKwh()) {
                eq.setStatus("ALERTA");
                repository.save(eq);
                return "ALERTA: Consumo (" + consumoAtual + " kWh) excedeu o limite de " + eq.getLimiteConsumoKwh() + " kWh!";
            } else {
                if (eq.getStatus().equals("ALERTA")) {
                    eq.setStatus("ATIVO");
                    repository.save(eq);
                }
                return "Consumo normal: " + consumoAtual + " kWh.";
            }
        }
        return "Equipamento não encontrado.";
    }
}