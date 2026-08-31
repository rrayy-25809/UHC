package com.aqua.uhc;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import net.kyori.adventure.text.Component;

public class GameScoreboard implements Listener {
    private final UHC plugin;
    private final World gameWorld;
    private final Scoreboard scoreboard;
    private final Objective objective;
    private final Team linePlayers;
    private final Team lineBorder;

    // 고정 entry 문자열 (색코드로 보이지 않는 고유 entry)
    private static final String ENTRY_PLAYERS = "§a";
    private static final String ENTRY_SPACER = "§b";
    private static final String ENTRY_BORDER = "§c";

    public GameScoreboard(UHC plugin, World gameWorld) {
        this.plugin = plugin;
        this.gameWorld = gameWorld;

        ScoreboardManager manager = plugin.getServer().getScoreboardManager();
        this.scoreboard = manager.getNewScoreboard();

        this.objective = scoreboard.registerNewObjective("uhc_game", Criteria.DUMMY, Component.text("§6UHC 게임"));
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Team 기반 고정 라인 - prefix/suffix 업데이트로 플리커 방지
        this.linePlayers = scoreboard.registerNewTeam("uhc_players");
        this.linePlayers.addEntry(ENTRY_PLAYERS);

        Team spacerTeam = scoreboard.registerNewTeam("uhc_spacer");
        spacerTeam.addEntry(ENTRY_SPACER);
        spacerTeam.prefix(Component.text(" "));

        this.lineBorder = scoreboard.registerNewTeam("uhc_border");
        this.lineBorder.addEntry(ENTRY_BORDER);

        // 초기 배치 (고정 score 값)
        this.objective.getScore(ENTRY_PLAYERS).setScore(2);
        this.objective.getScore(ENTRY_SPACER).setScore(1);
        this.objective.getScore(ENTRY_BORDER).setScore(0);

        // 초기 값 렌더링
        updateScoreboard();

        // 이미 월드에 있는 플레이어에게 적용
        for (Player player : gameWorld.getPlayers()) {
            player.setScoreboard(scoreboard);
        }

        // 신규 입장 플레이어 자동 적용을 위해 리스너 등록
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void updateScoreboard() {
        if (objective == null) return;
        int currentPlayers = getTotalSurvivalPlayers();
        double borderSize = gameWorld.getWorldBorder().getSize();
        linePlayers.prefix(Component.text("§f남은 인원: " + currentPlayers));
        lineBorder.prefix(Component.text("§e자기장 크기: §f" + (int) borderSize));
    }

    public void applyTo(Player player) {
        if (player.getWorld().equals(gameWorld)) {
            player.setScoreboard(scoreboard);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // 접속 시 게임 월드에 있으면 스코어보드 적용 (지연 없이)
        Player player = event.getPlayer();
        if (player.getWorld().equals(gameWorld)) {
            applyTo(player);
        }
    }

    @EventHandler
    public void onWorldChanged(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld().equals(gameWorld)) {
            applyTo(player);
        }
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
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
