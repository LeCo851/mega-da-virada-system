package br.com.leandrocoelho.megaapi.config;

import br.com.leandrocoelho.megacommon.dto.BetRequest;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, BetRequest> producerFactory(KafkaProperties properties){
        Map<String, Object> props = properties.buildProducerProperties(null);

        //serializers
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        //batch size 64kb
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, "65536");
        //linger MS: espera 20ms para encher o lote
        props.put(ProducerConfig.LINGER_MS_CONFIG, "20");

        props.put(ProducerConfig.ACKS_CONFIG, "1");

        return new DefaultKafkaProducerFactory<>(props);
    }
    @Bean
    public KafkaTemplate<String, BetRequest> kafkaTemplate(ProducerFactory<String ,BetRequest> producerFactory){
        return new KafkaTemplate<>(producerFactory);
    }

}
