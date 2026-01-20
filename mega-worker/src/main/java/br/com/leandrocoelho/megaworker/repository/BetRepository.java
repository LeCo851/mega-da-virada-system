package br.com.leandrocoelho.megaworker.repository;


import br.com.leandrocoelho.megaworker.model.BetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BetRepository extends JpaRepository<BetEntity, Long> {
}
