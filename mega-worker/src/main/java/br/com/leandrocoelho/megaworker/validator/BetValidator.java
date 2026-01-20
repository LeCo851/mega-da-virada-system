package br.com.leandrocoelho.megaworker.validator;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class BetValidator {

    public String validate(List<Integer> numbers){

        if(numbers.size() < 6 || numbers.size() >15){
            return "INVALID_SIZE";
        }

        boolean hasInvalidRange = numbers.stream()
                .anyMatch(n -> n < 1 || n >60);
        if(hasInvalidRange){
            return  "INVALID_RANGE";
        }

        Set<Integer> uniqueNumbers = new HashSet<>(numbers);
        if (uniqueNumbers.size() != numbers.size()){
            return "INVALID_DUPLICATE";
        }
        return "PROCESSED";
    }
}
