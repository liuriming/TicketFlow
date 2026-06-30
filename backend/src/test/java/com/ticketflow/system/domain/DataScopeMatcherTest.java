package com.ticketflow.system.domain;

import com.ticketflow.system.enums.DataScopeType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DataScopeMatcherTest {

    @Test
    void 本人数据范围只允许访问自己创建的数据() {
        boolean allowed = DataScopeMatcher.canAccess(
                DataScopeType.SELF,
                10L,
                20L,
                "1/2",
                "1/2"
        );

        assertThat(allowed).isFalse();
    }

    @Test
    void 本部门及下级允许访问子部门数据() {
        boolean allowed = DataScopeMatcher.canAccess(
                DataScopeType.DEPT_AND_CHILD,
                10L,
                20L,
                "1/2",
                "1/2/5"
        );

        assertThat(allowed).isTrue();
    }
}
