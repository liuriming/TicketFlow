package com.ticketflow.common.cache;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TicketFlowCacheKeysTest {

    @Test
    void 缓存Key覆盖登录权限字典和热点统计场景() {
        assertThat(TicketFlowCacheKeys.loginToken("abc")).isEqualTo("ticketflow:login:abc");
        assertThat(TicketFlowCacheKeys.permissionUser(7L)).isEqualTo("ticketflow:permission:user:7");
        assertThat(TicketFlowCacheKeys.dictionary("ticket-category")).isEqualTo("ticketflow:dict:ticket-category");
        assertThat(TicketFlowCacheKeys.hotStats("report:overview:user:7")).isEqualTo("ticketflow:stats:report:overview:user:7");
    }
}
