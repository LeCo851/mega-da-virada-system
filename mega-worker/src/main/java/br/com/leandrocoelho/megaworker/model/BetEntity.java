package br.com.leandrocoelho.megaworker.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tb_bets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @ElementCollection(fetch =  FetchType.EAGER)
    @CollectionTable(name = "tb_bet_numbers", joinColumns =  @JoinColumn(name = "bet_id"))
    private List<Integer> numbers;

    private LocalDateTime createdAt;

    @Builder.Default
    private String status = "PROCESSED";
}
