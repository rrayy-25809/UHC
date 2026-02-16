package com.aqua.uhc;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Criteria;

import net.kyori.adventure.text.Component;

public class GameScoreboard {
    private UHC plugin;
    private World gameWorld;
    private Objective playerCountObjective;

    public GameScoreboard(UHC plugin, World gameWorld) {
        this.plugin = plugin;
        this.gameWorld = gameWorld;
        this.setupScoreboard();
    }

    private void setupScoreboard() {
        ScoreboardManager scoreboardManager = plugin.getServer().getScoreboardManager();
        Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
        
        // 게임 정보 표시
        this.playerCountObjective = scoreboard.registerNewObjective("uhc_game", Criteria.DUMMY, Component.text("§6UHC 게임"));
        this.playerCountObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        // 초기 값 설정
        this.playerCountObjective.getScore("§f남은 인원: " + this.getTotalSurvivalPlayers()).setScore(2);
        this.playerCountObjective.getScore(" ").setScore(1);
        double borderSize = gameWorld.getWorldBorder().getSize();
        this.playerCountObjective.getScore("§e자기장 크기: §f" + (int)borderSize).setScore(0);
        
        // 월드의 모든 플레이어에게 스코어보드 적용
        for (Player player : gameWorld.getPlayers()) {
            player.setScoreboard(scoreboard);
        }
    }

    public void updateScoreboard() {
        if (this.playerCountObjective != null) {
            // 기존 스코어 모두 제거
            for (String entry : this.playerCountObjective.getScoreboard().getEntries()) {
                this.playerCountObjective.getScoreboard().resetScores(entry);
            }
            
            // 새로운 정보 추가
            int currentPlayers = this.getTotalSurvivalPlayers();
            this.playerCountObjective.getScore("§f남은 인원: " + currentPlayers).setScore(2);
            this.playerCountObjective.getScore(" ").setScore(1);
            double borderSize = gameWorld.getWorldBorder().getSize();
            this.playerCountObjective.getScore("§e자기장 크기: §f" + (int)borderSize).setScore(0);
        }
    }

    private int getTotalSurvivalPlayers() {
        int count = 0;
        for (Player player : gameWorld.getPlayers()) {
            if (player.getGameMode() == GameMode.SURVIVAL) {
                count++;
            }
        }
        return count;
    }
}
