package com.aqua.uhc;

import org.bukkit.GameRules;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.aqua.uhc.util.TimeUnit;

import net.kyori.adventure.text.Component;

public class timeManager { //TODO: 스코어보드 띄우기, 플레이어 죽음 감지, 추가되는 기능에 따라 클래스 이름 변경에 대해 고민하기
    private UHC plugin;
    private BukkitRunnable GameRunnable;
    private Long times = 0L;

    /**
     * @param plugin 플러그인
     * @param world 게임 실행할 월드
     * @param GatheringTime
     * @param PVPTime
     * @param DeathmatchTime
     */
    public timeManager(UHC plugin, World world, Long GatheringTime, Long PVPTime) {
        if (world==null) {
            throw new NullPointerException("world 변수는 null 일 수 없습니다.");
        }
        WorldBorder border = world.getWorldBorder();
        border.setCenter(0, 0);
        border.setSize(1000);
        this.plugin = plugin;

        this.GameRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (times <= GatheringTime/TimeUnit.SECEND) { // 자원 모으는 시간일 때
                    if (times % 60 == 0) { // 1 분마다
                        world.sendMessage(Component.text("PVP시간까지 남은 시간은 " + (int) (GatheringTime/TimeUnit.SECEND - times)/60 + "분 입니다"));
                    }

                    if (times == GatheringTime/TimeUnit.SECEND) {
                        world.setGameRule(GameRules.PVP, true);
                        border.changeSize(16, PVPTime); // 마크 내에서 점차 줄어들게 설정, 대신 줄어들었을 때 다음 작업 실행은 불가
                        world.sendMessage(Component.text("이제 월드 보더가 점점 축소됩니다!"));
                    }
                } else if (times <= (GatheringTime + PVPTime)/TimeUnit.SECEND) { // 싸우는 시간일 때
                    if (times % 60 == 0) { // 1 분마다
                        world.sendMessage(Component.text("데스매치까지 남은 시간은 " + (int) ((GatheringTime+ PVPTime)/TimeUnit.SECEND - times)/60 + "분 입니다"));
                    }

                    if (times == (GatheringTime+ PVPTime)/TimeUnit.SECEND) {
                        world.sendMessage(Component.text("이제 모든 플레이어는 발광 효과를 얻습니다."));
                    }
                } else { // 데스메치 시간일 때
                    world.getPlayers().forEach((player) -> {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 1, true, false));
                    });
                }

                times += 1;
            }
        };
    }

    public void run() {
        this.GameRunnable.runTaskTimer(this.plugin, 0, TimeUnit.SECEND);
    }

    public void stop() {
        this.GameRunnable.cancel();
    }
}
