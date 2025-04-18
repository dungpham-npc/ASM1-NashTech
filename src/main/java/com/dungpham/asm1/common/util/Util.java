package com.dungpham.asm1.common.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Util {
    public static LocalDateTime convertTimestampToLocalDateTime(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp),
                ZoneId.systemDefault()
        );
    }
}