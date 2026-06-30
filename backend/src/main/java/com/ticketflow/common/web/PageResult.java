package com.ticketflow.common.web;

import java.util.List;

/**
 * 分页查询统一响应结构。
 *
 * @param records 当前页数据列表。
 * @param total 总记录数。
 * @param pageNo 当前页码，从 1 开始。
 * @param pageSize 每页记录数。
 * @param <T> 列表数据类型。
 */
public record PageResult<T>(List<T> records, long total, long pageNo, long pageSize) {
}
