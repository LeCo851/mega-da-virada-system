package br.com.leandrocoelho.megaworker.consumer;

import br.com.leandrocoelho.megacommon.dto.BetRequest;
import br.com.leandrocoelho.megaworker.model.BetEntity;
import br.com.leandrocoelho.megaworker.repository.BetRepository;
import br.com.leandrocoelho.megaworker.validator.BetValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaBatchConsumer {
    private final BetRepository betRepository;
    private final BetValidator betValidator;

    @KafkaListener(topics = "${app.kafka.topic:bets-topic}", groupId = "mega-worker")
    public void ConsumeBatch(List<BetRequest> betRequest){

        long start = System.currentTimeMillis();
        List<BetEntity> betsToSave = new ArrayList<>(betRequest.size());

        for (BetRequest request : betRequest){
            String status = betValidator.validate(request.numbers());

            if("PROCESSED".equals(status)){
                betsToSave.add(BetEntity.builder()
                                .userId(request.userId())
                                .numbers(request.numbers())
                                .createdAt(LocalDateTime.now())
                                .status(status)
                                .build());
            }
        }

        if(!betsToSave.isEmpty()){
            betRepository.saveAll(betsToSave);
        }

        log.info("Batch processado: {} itens em {} ms", betRequest.size(),(System.currentTimeMillis() - start));
    }

}
