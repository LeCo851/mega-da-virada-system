package br.com.leandrocoelho.megaapi.controller;

import br.com.leandrocoelho.megaapi.service.api.BetProducerStrategy;
import br.com.leandrocoelho.megacommon.dto.BetRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/bets")
@RequiredArgsConstructor
class BetController {

    private final BetProducerStrategy kafkaBetProducer;
    private final BetProducerStrategy redisBetProducer;

    @PostMapping("/kafka")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<Void> createBetKafka(@RequestBody @Valid BetRequest betRequest){
        kafkaBetProducer.sendMessage(betRequest);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/redis")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<Void> createBetRedis(@RequestBody @Valid BetRequest betRequest){
         redisBetProducer.sendMessage(betRequest);
         return ResponseEntity.accepted().build();
    }
}
