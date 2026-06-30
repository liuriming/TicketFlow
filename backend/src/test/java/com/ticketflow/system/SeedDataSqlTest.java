package com.ticketflow.system;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SeedDataSqlTest {

    @Test
    void 初始化数据包含四类角色演示账号和默认派单规则() throws Exception {
        String sql = Files.readString(Path.of("src/main/resources/data.sql"), StandardCharsets.UTF_8);

        assertThat(sql).contains("'employee'");
        assertThat(sql).contains("'ops_engineer'");
        assertThat(sql).contains("'ops_manager'");
        assertThat(sql).contains("INSERT IGNORE INTO dispatch_rule");
        assertThat(sql).contains("'NETWORK'");
        assertThat(sql).contains("'ACCOUNT'");
        assertThat(sql).contains("'SERVER'");
        assertThat(sql).contains("'ticket:transfer'");
    }
}
