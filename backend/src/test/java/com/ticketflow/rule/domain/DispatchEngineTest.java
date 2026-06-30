package com.ticketflow.rule.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DispatchEngineTest {

    @Test
    void 优先选择技能匹配且负载更低的运维人员() {
        DispatchContext context = new DispatchContext(100L, 2L, "NETWORK");
        DispatchCandidate busyMatch = new DispatchCandidate(10L, 2L, List.of("NETWORK"), 5);
        DispatchCandidate idleMatch = new DispatchCandidate(11L, 2L, List.of("NETWORK", "SERVER"), 1);
        DispatchCandidate wrongSkill = new DispatchCandidate(12L, 2L, List.of("DATABASE"), 0);

        DispatchCandidate selected = DispatchEngine.selectBest(context, List.of(busyMatch, idleMatch, wrongSkill))
                .orElseThrow();

        assertThat(selected.userId()).isEqualTo(11L);
    }
}
