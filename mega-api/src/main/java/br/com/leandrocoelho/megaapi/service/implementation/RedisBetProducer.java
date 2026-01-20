package br.com.leandrocoelho.megaapi.service.implementation;

import br.com.leandrocoelho.megaapi.service.api.BetProducerStrategy;
import br.com.leandrocoelho.megacommon.dto.BetRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;


@Slf4j
@Service
@RequiredArgsConstructor
public class RedisBetProducer implements BetProducerStrategy {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String QUEUE_KEY = "bets_queue";

    @Override
    public void sendMessage(BetRequest betRequest){
        try{
            redisTemplate.opsForList().leftPush(QUEUE_KEY, betRequest);
        }catch (Exception e){
            log.error("Erro ao enviar aposta para o Redis", e);
            throw e;
        }
    }
}
