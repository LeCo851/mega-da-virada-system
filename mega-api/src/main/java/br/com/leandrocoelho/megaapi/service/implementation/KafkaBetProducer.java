package br.com.leandrocoelho.megaapi.service.implementation;

import br.com.leandrocoelho.megaapi.service.api.BetProducerStrategy;
import br.com.leandrocoelho.megacommon.dto.BetRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaBetProducer implements BetProducerStrategy {

    private final KafkaTemplate<String, BetRequest> kafkaTemplate;

    @Value("${app.kafka.topic:bets-topic}")
    private String topicName;

    @Override
    public void sendMessage(BetRequest betRequest){
        kafkaTemplate.send(topicName, betRequest)
                .whenComplete((result, ex) ->{
                    if(ex != null){
                        log.error("Erro no envio ao Kafka: {}", ex.getMessage());
                    }else{
                        log.debug("Enviado. Offset: {}", result.getRecordMetadata());
                    }
                });
    }
}
