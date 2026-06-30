package com.ticketflow.attachment.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AttachmentObjectNameBuilderTest {

    @Test
    void 根据日期随机串和原始文件名生成可追踪对象名() {
        String objectName = AttachmentObjectNameBuilder.build(
                "故障截图.png",
                LocalDate.of(2026, 6, 30),
                () -> "abc123"
        );

        assertThat(objectName).isEqualTo("2026/06/30/abc123-故障截图.png");
    }
}
