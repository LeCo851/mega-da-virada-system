package br.com.leandrocoelho.megaworker.consumer;

import br.com.leandrocoelho.megacommon.dto.BetRequest;
import br.com.leandrocoelho.megaworker.model.BetEntity;
import br.com.leandrocoelho.megaworker.repository.BetRepository;
import br.com.leandrocoelho.megaworker.validator.BetValidator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisBatchConsumer {

    private final RedisTemplate<String , Object> redisTemplate;
    private final BetRepository betRepository;
    private final BetValidator betValidator;

    private static final String QUEUE_NAME = "bets_queue";
    private static final int BATCH_SIZE = 500;
    private static final int CONSUMER_THREADS = 20;
    private volatile boolean running = true;

    @PostConstruct
    public void startConsumer(){
        for (int i =0; i <CONSUMER_THREADS; i++) {
            new Thread(this::runLoop, "Redis-Consumer-" + i).start();
        }
    }
    @jakarta.annotation.PreDestroy
    public void stopConsumer() {
        log.info("🛑 Parando consumidores Redis...");
        running = false; // Avisa o loop para parar
    }

    private void runLoop(){
        log.info("Redis Batch consumer online");

        while (running){
            try {
                List<Object> rawbets = redisTemplate.opsForList().rightPop(QUEUE_NAME, BATCH_SIZE);

                if(rawbets == null || rawbets.isEmpty()){
                    TimeUnit.MILLISECONDS.sleep(50);
                    continue;
                }
                processBatch(rawbets);
            } catch (Exception e) {
                log.error("Erro no loop do Redis", e);

                try{TimeUnit.SECONDS.sleep(1);} catch (InterruptedException ex) {Thread.currentThread().interrupt();}
            }
        }
    }
    private void processBatch(List<Object> rawBets){
        long start = System.currentTimeMillis();
        List<BetEntity> betsToSave = new ArrayList<>(rawBets.size());
        LocalDateTime batchTime = LocalDateTime.now();

        for(Object obj : rawBets){

            if(obj instanceof BetRequest betRequest){
                String status = betValidator.validate(betRequest.numbers());

                if("PROCESSED".equals(status)){
                    betsToSave.add(BetEntity.builder()
                                    .userId(betRequest.userId())
                                    .numbers(betRequest.numbers())
                                    .createdAt(batchTime)
                                    .status(status)
                                    .build());
                }
            }
        }
        if(!betsToSave.isEmpty()){
            betRepository.saveAll(betsToSave);
        }

        log.info("[REDIS] Lote processado: {} itens em {} ms", rawBets.size(),(System.currentTimeMillis() -start));
    }
}
