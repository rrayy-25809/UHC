package com.aqua.uhc;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.scheduler.BukkitRunnable;

public final class UHC extends JavaPlugin {
    public int t;
    private worldborder border_manager;
    public World world;
    public WorldBorder border;

    public void wb(){
        // 2. 반복 작업 생성 (BukkitRunnable)
        new BukkitRunnable() {
            @Override
            public void run() {
                double currentSize = border.getSize();
                double minSize = 100; // 최소 크기 제한
                double shrinkAmount = 10; // 한 번에 줄어들 양
                if (currentSize > minSize) {
                    // 60초(1200틱) 동안 부드럽게 shrinkAmount만큼 줄어들게 설정
                    border.setSize(currentSize - shrinkAmount, 5);
                    // 서버 전체에 알림 (선택 사항)
                    Bukkit.broadcastMessage("§c[경고] §f월드 보더가 축소되었습니다! 현재 크기: " + (int)(currentSize - shrinkAmount));
                } else {
                        // 최소 크기에 도달하면 반복 중지
                    this.cancel();
                }
            }
        }.runTaskTimer(this, 0L, 100L); // 1200틱(1분)마다 실행
    }

    public void Timer(int time, int msgdelay){
        t=time;
        new BukkitRunnable() {
            public void run(){
                if(t%msgdelay==0) {
                    Bukkit.broadcastMessage("남은 시간은 " + t + "초 입니다");
                }
                if(t==0) {
                    wb();
                    this.cancel();
                }
                t=t-1;
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    public void onEnable() {
        getLogger().info("UHC 플러그인이 활성화 되었습니다!");
        // 보더 축소를 관리할 월드 설정 (기본값: "world")
        world = Bukkit.getWorld("world");
        if (world != null) {
            border = world.getWorldBorder();
            border.setCenter(0, 0);
            border.setSize(4000);
            Timer(30,5);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("UHC 플러그인이 비활성화 되었습니다!");
    }
}
