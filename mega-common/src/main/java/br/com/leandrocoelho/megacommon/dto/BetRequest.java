package br.com.leandrocoelho.megacommon.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record BetRequest
        (
                @NotNull(message = "O ID do usuário é obrigatório")
                Long userId,
                @NotNull(message = "A lista de números não pode ser nula")
                @Size(min = 6, max = 15, message = "A aposta deve ter entre 6 e 15 números")
                List<Integer> numbers
        ) {}
