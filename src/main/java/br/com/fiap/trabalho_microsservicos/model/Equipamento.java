package br.com.fiap.trabalho_microsservicos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_equipamentos")
public class Equipamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do equipamento é obrigatório")
    private String nome;

    @NotBlank(message = "O setor é obrigatório")
    private String setor;

    @NotNull(message = "O limite de consumo deve ser definido")
    @Positive(message = "O limite deve ser maior que zero")
    @Column(name = "limite_consumo_kwh")
    private Double limiteConsumoKwh;

    private String status; // ATIVO, OCIOSO, DESLIGADO
}