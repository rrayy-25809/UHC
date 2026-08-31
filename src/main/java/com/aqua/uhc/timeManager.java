package com.aqua.uhc;

import org.bukkit.GameRules;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.aqua.uhc.util.Ticks;

import net.kyori.adventure.text.Component;

public class TimeManager {
    private final UHC plugin;
    private final World world;
    private final WorldBorder border;
    private final GameScoreboard gameScoreboard;
    private final long gatheringTimeTicks;
    private final long pvpTimeTicks;
    private final long gatheringSeconds;
    private final long pvpSeconds;
    private final long totalPhaseSeconds;

    private BukkitRunnable gameRunnable;
    private long elapsedSeconds = 0L;

    /**
     * @param plugin 플러그인
     * @param world 게임 월드
     * @param gatheringTimeTicks 자원 모으는 시간 (tick 단위)
     * @param pvpTimeTicks 싸우는 시간 (tick 단위)
     * @param gameScoreboard 게임 스코어보드
     */
    public TimeManager(UHC plugin, World world, long gatheringTimeTicks, long pvpTimeTicks, GameScoreboard gameScoreboard) {
        if (world == null) {
            throw new NullPointerException("world 변수는 null 일 수 없습니다.");
        }
        this.plugin = plugin;
        this.world = world;
        this.border = world.getWorldBorder();
        this.border.setCenter(0, 0);
        this.border.setSize(1000);
        this.gameScoreboard = gameScoreboard;
        this.gatheringTimeTicks = gatheringTimeTicks;
        this.pvpTimeTicks = pvpTimeTicks;
        this.gatheringSeconds = gatheringTimeTicks / Ticks.SECOND;
        this.pvpSeconds = pvpTimeTicks / Ticks.SECOND;
        this.totalPhaseSeconds = gatheringSeconds + pvpSeconds;

        this.gameRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (elapsedSeconds % 10 == 0) {
                    gameScoreboard.updateScoreboard();
                }

                if (elapsedSeconds <= gatheringSeconds) {
                    if (elapsedSeconds % 60 == 0) {
                        long remaining = gatheringSeconds - elapsedSeconds;
                        world.sendMessage(Component.text("PVP까지 남은 시간: " + (remaining / 60) + "분"));
                    }
                    if (elapsedSeconds == gatheringSeconds) {
                        world.setGameRule(GameRules.PVP, true);
                        //noinspection deprecation
                        border.setSize(16, pvpSeconds);
                        world.sendMessage(Component.text("이제 월드 보더가 점점 축소됩니다!"));
                    }
                } else if (elapsedSeconds <= totalPhaseSeconds) {
                    if (elapsedSeconds % 60 == 0) {
                        long remaining = totalPhaseSeconds - elapsedSeconds;
                        world.sendMessage(Component.text("데스매치까지 남은 시간: " + (remaining / 60) + "분"));
                    }
                    if (elapsedSeconds == totalPhaseSeconds) {
                        world.sendMessage(Component.text("이제 모든 플레이어는 발광 효과를 얻습니다."));
                    }
                } else {
                    world.getPlayers().forEach(player -> {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 220, 0, true, false, false));
                    });
                }

                elapsedSeconds++;
            }
        };
    }

    public void run() {
        this.gameRunnable.runTaskTimer(this.plugin, 0, Ticks.SECOND);
    }

    public void stop() {
        if (gameRunnable != null) {
            try {
                gameRunnable.cancel();
            } catch (IllegalStateException ignored) {
            }
        }
    }

    public long getElapsedSeconds() {
        return elapsedSeconds;
    }
}
