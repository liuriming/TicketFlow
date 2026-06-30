package com.ticketflow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 企业工单协作系统后端启动类。
 *
 * <p>当前项目采用模块化单体结构，所有模块运行在同一个 Spring Boot 进程中，
 * 通过包边界区分系统权限、工单流程、派单规则、SLA、通知消息、附件存储和统计报表。</p>
 */
@EnableScheduling
@SpringBootApplication
@MapperScan("com.ticketflow.**.mapper")
public class TicketFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketFlowApplication.class, args);
    }
}
