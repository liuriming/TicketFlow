package com.ticketflow.sla.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SlaCalculatorTest {

    @Test
    void 根据创建时间和规则分钟数计算响应与处理截止时间() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 6, 30, 9, 0);

        SlaDeadline deadline = SlaCalculator.calculate(createdAt, 30, 240);

        assertThat(deadline.responseDeadline()).isEqualTo(LocalDateTime.of(2026, 6, 30, 9, 30));
        assertThat(deadline.resolveDeadline()).isEqualTo(LocalDateTime.of(2026, 6, 30, 13, 0));
    }

    @Test
    void 当前时间晚于处理截止时间时判定处理超时() {
        SlaDeadline deadline = new SlaDeadline(
                LocalDateTime.of(2026, 6, 30, 9, 30),
                LocalDateTime.of(2026, 6, 30, 13, 0)
        );

        assertThat(SlaCalculator.isResolveOverdue(deadline, LocalDateTime.of(2026, 6, 30, 13, 1)))
                .isTrue();
    }
}
