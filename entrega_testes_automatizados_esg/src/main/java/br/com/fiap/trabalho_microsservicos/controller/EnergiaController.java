package br.com.fiap.trabalho_microsservicos.controller;

import br.com.fiap.trabalho_microsservicos.model.Equipamento;
import br.com.fiap.trabalho_microsservicos.service.EquipamentoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/energia")
public class EnergiaController {

    @Autowired
    private EquipamentoService service;

    @PostMapping("/equipamentos")
    public ResponseEntity<Equipamento> cadastrarEquipamento(@Valid @RequestBody Equipamento equipamento) {
        return new ResponseEntity<>(service.cadastrarEquipamento(equipamento), HttpStatus.CREATED);
    }

    @GetMapping("/equipamentos")
    public ResponseEntity<List<Equipamento>> listarEquipamentos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/alertas")
    public ResponseEntity<List<Equipamento>> listarAlertas() {
        return ResponseEntity.ok(service.listarAlertas());
    }

    @PutMapping("/equipamentos/{id}/desligar")
    public ResponseEntity<String> desligarEquipamento(@PathVariable Long id) {
        boolean desligado = service.desligarEquipamento(id);
        if (desligado) {
            return ResponseEntity.ok("Equipamento " + id + " desligado com sucesso via IoT.");
        }
        return ResponseEntity.notFound().build();
    }


    @GetMapping("/estatisticas")
    public ResponseEntity<Map<String, Long>> obterEstatisticas() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("ativos", service.contarPorStatus("ATIVO"));
        stats.put("ociosos", service.contarPorStatus("OCIOSO"));
        stats.put("desligados", service.contarPorStatus("DESLIGADO"));
        return ResponseEntity.ok(stats);
    }
}