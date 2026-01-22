package com.aqua.uhc;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRules;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import com.aqua.uhc.util.TimeUnit;

public class UHC extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("UHC 플러그인이 활성화 되었습니다!");
        // 보더 축소를 관리할 월드 설정 (기본값: "world")
        World world = Bukkit.getWorld("world");
        world.setDifficulty(Difficulty.NORMAL);
        world.setGameRule(GameRules.ADVANCE_TIME, false);
        world.setGameRule(GameRules.IMMEDIATE_RESPAWN, true);
        getServer().getPluginManager().registerEvents(new gameEvent(world), this);
        timeManager timeManager = new timeManager(this, world, 15*TimeUnit.MINUTE, TimeUnit.HOUR);
        timeManager.run();
    }

    @Override
    public void onDisable() {
        getLogger().info("UHC 플러그인이 비활성화 되었습니다!");
    }
}
