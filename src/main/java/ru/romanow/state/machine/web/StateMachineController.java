package ru.romanow.state.machine.web;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.romanow.state.machine.service.CalculationService;

@RequestMapping("/api/v1")
@RestController
@RequiredArgsConstructor
public class StateMachineController {

    private final CalculationService calculationService;

    @GetMapping("/next-state/{calculationUid}")
    public String nextState(@PathVariable UUID calculationUid) {
        return calculationService.nextState(calculationUid);
    }

}
