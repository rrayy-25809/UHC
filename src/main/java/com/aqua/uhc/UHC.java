package com.aqua.uhc;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.kyori.adventure.text.Component;

import java.util.Random;

public class UHC extends JavaPlugin implements Listener {
    public WorldBorder border;
    private int t;
    private final Random random = new Random();

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
        getServer().getPluginManager().registerEvents(new UHC(), this);
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

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // 1. 기본 조건 검사 (서바이벌 여부, 섬세한 손길 여부)
        if (player.getGameMode() != GameMode.SURVIVAL) return;
        if (itemInHand.containsEnchantment(Enchantment.SILK_TOUCH)) return;

        Material resultMaterial = null;
        int expToDrop = 0;

        // 2. 광물 종류 판정
        switch (block.getType()) {
            case IRON_ORE:
            case DEEPSLATE_IRON_ORE:
            case RAW_IRON_BLOCK:
                resultMaterial = Material.IRON_INGOT;
                expToDrop = 1;
                break;
            case GOLD_ORE:
            case DEEPSLATE_GOLD_ORE:
            case RAW_GOLD_BLOCK:
            case NETHER_GOLD_ORE:
                resultMaterial = Material.GOLD_INGOT;
                expToDrop = 1;
                break;
            case COPPER_ORE:
            case DEEPSLATE_COPPER_ORE:
                resultMaterial = Material.COPPER_INGOT;
                expToDrop = 1;
                break;
            case ANCIENT_DEBRIS:
                // 고대 잔해는 일반적으로 행운의 영향을 받지 않음
                resultMaterial = Material.NETHERITE_SCRAP;
                expToDrop = 2;
                break;
            default:
                return;
        }

        if (resultMaterial != null) {
            event.setDropItems(false); // 기존 아이템 드롭 방지

            // 3. 행운(Fortune) 로직 계산
            int amount = 1;
            int fortuneLevel = itemInHand.getEnchantmentLevel(Enchantment.FORTUNE);

            if (fortuneLevel > 0 && block.getType() != Material.ANCIENT_DEBRIS) {
                // 마인크래프트 공식 행운 확률 공식 시뮬레이션
                // 행운 레벨 n일 때, (1/n+2) 확률로 각각 1, 2, ..., n+1배 드롭
                int r = random.nextInt(fortuneLevel + 2);
                amount = Math.max(1, r);

                // 구리 광물의 경우 기본 드롭량이 많으므로 별도 보정이 필요할 수 있으나,
                // 여기서는 주괴로 변환하므로 일반적인 행운 로직을 적용했습니다.
            }

            // 4. 아이템 및 경험치 드롭
            ItemStack dropItem = new ItemStack(resultMaterial, amount);
            block.getWorld().dropItemNaturally(block.getLocation(), dropItem);

            if (expToDrop > 0) {
                block.getWorld().spawn(block.getLocation(), ExperienceOrb.class).setExperience(expToDrop);
            }
        }
    }
}
