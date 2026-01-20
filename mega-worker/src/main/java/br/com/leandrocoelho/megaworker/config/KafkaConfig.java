package br.com.leandrocoelho.megaworker.config;

import br.com.leandrocoelho.megacommon.dto.BetRequest;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.kafka.core.MicrometerConsumerListener;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public ConsumerFactory<String, BetRequest> consumerFactory(KafkaProperties properties, MeterRegistry meterRegistry){
        Map<String, Object> props = properties.buildConsumerProperties(null);
        //chave é texto
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // valor é JSON
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserialize.class);
        //confiar no pacote recebido como JSON
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");


         DefaultKafkaConsumerFactory<String, BetRequest> factory = new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(BetRequest.class, false)
        );
         factory.addListener(new MicrometerConsumerListener<>(meterRegistry));
         return factory;
    }
    @Bean
    ConcurrentKafkaListenerContainerFactory<String, BetRequest> kafkaListenerContainerFactory(ConsumerFactory<String, BetRequest> consumerFactory){
        ConcurrentKafkaListenerContainerFactory<String, BetRequest> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        factory.setBatchListener(true);
        factory.setConcurrency(20);
        return factory;
    }
}
