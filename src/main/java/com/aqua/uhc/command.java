package com.aqua.uhc;

import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRules;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.core.world.options.CreateWorldOptions;

import com.aqua.uhc.util.TimeUnit;

public class command implements CommandExecutor {
    private UHC plugin;
    private WorldManager worldManager;

    public command(UHC plugin) {
        this.plugin = plugin;
        this.worldManager = plugin.coreApi.getWorldManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        // 현재 로드된 모든 월드 정보를 로그에 출력
        worldManager.getLoadedWorlds().forEach(world -> {
            this.plugin.getLogger().info("로드된 월드: " + world.getName()); // 로그 확인하니까 world, world__nether, world_the_end 도 출력됨
        });
        
        // 새로운 게임 월드 생성 요청 (비동기 작업)
        worldManager.createWorld(CreateWorldOptions.worldName("test_game")
                .environment(Environment.NORMAL)
                .worldType(WorldType.NORMAL)
                .generateStructures(true))
        // 월드 생성 실패 시 실행될 콜백
        .onFailure(reason -> {
            sender.sendMessage("실패!");
            this.plugin.getLogger().severe("월드 생성 실패: " + reason);
        })
        // 월드 생성 성공 시 실행될 콜백
        .onSuccess(world -> {
            // Multiverse 월드 객체에서 Bukkit 월드 객체 추출
            World bukkitWorld = world.getBukkitWorld().get();

            // 게임 월드 설정
            world.setPvp(false);  // PvP 비활성화
            world.setDifficulty(Difficulty.NORMAL);  // 난이도 설정
            world.setGameMode(GameMode.SURVIVAL);  // 게임 모드를 서바이벌로 설정
            world.setSpawnLocation(new Location(bukkitWorld, 0, 100, 0));  // 스폰 위치 설정
            
            // Bukkit 게임 규칙 설정
            bukkitWorld.setGameRule(GameRules.ADVANCE_TIME, false);  // 시간 경과 비활성화
            bukkitWorld.setGameRule(GameRules.IMMEDIATE_RESPAWN, true);  // 즉시 리스폰 활성화

            // 게임 이벤트 리스너 등록
            this.plugin.getServer().getPluginManager().registerEvents(new gameEvent(bukkitWorld), this.plugin);
            
            // 게임 스코어보드 생성
            GameScoreboard gameScoreboard = new GameScoreboard(plugin, bukkitWorld);
            
            // 시간 관리자 생성 및 실행
            timeManager timeManager = new timeManager(this.plugin, bukkitWorld, 15*TimeUnit.MINUTE, TimeUnit.HOUR, gameScoreboard);
            timeManager.run();

            // 명령어 실행자를 플레이어로 캐스팅하여 월드로 이동
            Player player = (Player) sender;
            player.teleport(world.getSpawnLocation());
        });
        return true;
    }
}
