package br.com.leandrocoelho.megaapi.service.api;
import  br.com.leandrocoelho.megacommon.dto.BetRequest;

public interface BetProducerStrategy {
    void sendMessage(BetRequest betRequest);

}
