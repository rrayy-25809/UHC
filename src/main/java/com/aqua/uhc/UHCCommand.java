package com.aqua.uhc;

import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRules;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.core.world.options.CreateWorldOptions;

import com.aqua.uhc.util.Ticks;

import net.kyori.adventure.text.Component;

public class UHCCommand implements CommandExecutor {
    private final UHC plugin;
    private final WorldManager worldManager;

    public UHCCommand(UHC plugin) {
        this.plugin = plugin;
        this.worldManager = plugin.coreApi.getWorldManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("§c이 명령어는 플레이어만 실행할 수 있습니다."));
            return true;
        }

        worldManager.getLoadedWorlds().forEach(world -> {
            plugin.getLogger().info("로드된 월드: " + world.getName());
        });

        worldManager.createWorld(CreateWorldOptions.worldName("test_game")
                .environment(Environment.NORMAL)
                .worldType(WorldType.NORMAL)
                .generateStructures(true))
        .onFailure(reason -> {
            player.sendMessage(Component.text("§c월드 생성 실패!"));
            plugin.getLogger().severe("월드 생성 실패: " + reason);
        })
        .onSuccess(mvWorld -> {
            var bukkitOpt = mvWorld.getBukkitWorld();
            if (bukkitOpt.isEmpty()) {
                player.sendMessage(Component.text("§cBukkit 월드 로드 실패"));
                plugin.getLogger().severe("Bukkit world is null for test_game");
                return;
            }
            World bukkitWorld = bukkitOpt.get();

            mvWorld.setPvp(false);
            mvWorld.setDifficulty(Difficulty.NORMAL);
            mvWorld.setGameMode(GameMode.SURVIVAL);

            // 안전한 스폰 위치 탐색
            Location spawn = findSafeSpawn(bukkitWorld);
            mvWorld.setSpawnLocation(spawn);
            bukkitWorld.setSpawnLocation(spawn);

            bukkitWorld.setGameRule(GameRules.ADVANCE_TIME, false);
            bukkitWorld.setGameRule(GameRules.IMMEDIATE_RESPAWN, true);

            GameEventListener gameEventListener = new GameEventListener(plugin, bukkitWorld);
            plugin.getServer().getPluginManager().registerEvents(gameEventListener, plugin);

            GameScoreboard gameScoreboard = new GameScoreboard(plugin, bukkitWorld);

            TimeManager timeManager = new TimeManager(plugin, bukkitWorld, 15 * Ticks.MINUTE, Ticks.HOUR, gameScoreboard);
            timeManager.run();

            player.teleport(spawn);
            // 텔레포트 후 초기 인원 보정 (생성 시점에는 0명이므로)
            // 1틱 지연 후 보정하여 teleport가 반영되도록 함
            plugin.getServer().getScheduler().runTask(plugin, gameEventListener::refreshInitialCount);

            player.sendMessage(Component.text("§aUHC 게임 월드 'test_game'가 생성되었습니다!"));
        });
        return true;
    }

    private Location findSafeSpawn(World world) {
        int x = 0, z = 0;
        int y = world.getHighestBlockYAt(x, z) + 1;
        // 최고 블록이 비정상적으로 낮거나 공중이면 100으로 fallback
        if (y < world.getMinHeight() + 2) y = 100;
        // 해당 위치가 안전하지 않으면 위로 탐색
        for (int i = 0; i < 10; i++) {
            Location loc = new Location(world, x + 0.5, y + i, z + 0.5);
            if (loc.getBlock().isEmpty() && loc.clone().add(0, 1, 0).getBlock().isEmpty()) {
                return loc;
            }
        }
        return new Location(world, x + 0.5, y, z + 0.5);
    }
}
