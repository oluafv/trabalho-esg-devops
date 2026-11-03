package br.com.fiap.trabalho_microsservicos.repository;

import br.com.fiap.trabalho_microsservicos.model.Equipamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipamentoRepository extends JpaRepository<Equipamento, Long> {

    List<Equipamento> findByStatus(String status);
}