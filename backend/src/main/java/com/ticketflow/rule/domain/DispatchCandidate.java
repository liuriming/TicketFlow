package com.ticketflow.rule.domain;

import java.util.List;

/**
 * 派单候选人。
 *
 * @param userId 运维人员用户 ID。
 * @param deptId 运维人员负责部门 ID。
 * @param skills 运维人员技能或负责范围编码集合。
 * @param currentLoad 当前未关闭工单数量，数值越低优先级越高。
 */
public record DispatchCandidate(Long userId, Long deptId, List<String> skills, int currentLoad) {
}
