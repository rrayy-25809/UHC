package com.aqua.uhc;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.kyori.adventure.text.Component;

public class UHC extends JavaPlugin {
    public WorldBorder border;
    private int t;

    public void Timer(int time, int msgdelay) {
        t = time;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (t % msgdelay == 0) {
                    Bukkit.broadcast(Component.text("남은 시간은 " + t + "초 입니다"));
                }
                if (t == 0) {
                    Bukkit.broadcast(Component.text("이제 월드 보더가 점점 축소됩니다!"));
                    border.changeSize(16, 60 * 60 * 20L); // 마크 내에서 점차 줄어들게 설정, 대신 줄어들었을 때 다음 작업 실행은 불가
                    this.cancel();
                }
                t = t - 1;
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    @Override
    public void onEnable() {
        getLogger().info("UHC 플러그인이 활성화 되었습니다!");
        getServer().getPluginManager().registerEvents(new event(), this);
        // 보더 축소를 관리할 월드 설정 (기본값: "world")
        World world = Bukkit.getWorld("world");
        if (world != null) {
            border = world.getWorldBorder();
            border.setCenter(0, 0);
            border.setSize(1000);
            Timer(15 * 60, 60);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("UHC 플러그인이 비활성화 되었습니다!");
    }
}
