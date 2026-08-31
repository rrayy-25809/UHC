package com.aqua.uhc.util;

/**
 * Tick 단위 상수 유틸리티.
 * Bukkit은 20틱 = 1초를 사용한다.
 * WorldBorder#changeSize 등 일부 API는 초 단위를 기대하므로 혼동에 주의.
 */
public final class Ticks {
    private Ticks() {}

    public static final long SECOND = 20L;
    public static final long MINUTE = 60 * SECOND;
    public static final long HOUR = 60 * MINUTE;

    public static long fromSeconds(long seconds) {
        return seconds * SECOND;
    }

    public static long toSeconds(long ticks) {
        return ticks / SECOND;
    }

    public static long fromMinutes(long minutes) {
        return minutes * MINUTE;
    }

    public static long fromHours(long hours) {
        return hours * HOUR;
    }
}
