package ru.romanow.state.machine.web;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.romanow.state.machine.domain.enums.CalculationType;
import ru.romanow.state.machine.service.TestTransitionService;

@RequestMapping("/api/v1")
@RestController
@RequiredArgsConstructor
public class StateMachineController {

    private final TestTransitionService transitionService;

    @GetMapping("/calculation/{type}/next-state/{calculationUid}")
    public List<String> nextState(@PathVariable String type, @PathVariable UUID calculationUid) {
        return transitionService.nextState(CalculationType.find(type), calculationUid);
    }
}
