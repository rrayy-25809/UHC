package com.aqua.uhc.util;

public interface TimeUnit {
    Long SECOND = 20L;
    Long MINUTE = 60 * SECOND;
    Long HOUR = 60 * MINUTE;

    /** @deprecated 오타 호환용. {@link #SECOND}를 사용하세요. */
    @Deprecated
    Long SECEND = SECOND;
}
