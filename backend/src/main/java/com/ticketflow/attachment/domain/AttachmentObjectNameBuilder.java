package com.ticketflow.attachment.domain;

import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

/**
 * 附件对象名生成器。
 *
 * <p>用于生成 MinIO 对象名称。对象名包含日期目录、随机串和原始文件名，
 * 既能降低同名文件冲突，也便于按日期排查文件。</p>
 */
public final class AttachmentObjectNameBuilder {

    private static final DateTimeFormatter DATE_PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private AttachmentObjectNameBuilder() {
    }

    /**
     * 生成对象名称。
     *
     * @param originalName 原始文件名。
     * @param date 上传日期。
     * @param randomSupplier 随机串供应器。
     * @return MinIO 对象名称。
     */
    public static String build(String originalName, LocalDate date, Supplier<String> randomSupplier) {
        String safeName = StringUtils.hasText(originalName) ? originalName.replace("\\", "_").replace("/", "_") : "unknown";
        return DATE_PATH_FORMATTER.format(date) + "/" + randomSupplier.get() + "-" + safeName;
    }
}
