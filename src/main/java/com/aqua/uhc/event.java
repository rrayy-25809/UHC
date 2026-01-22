package com.aqua.uhc;

import java.util.Random;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class event implements Listener {
    private final Random random = new Random();

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
                block.getWorld().spawn(block.getLocation(), ExperienceOrb.class).setExperience(expToDrop * amount);
            }
        }
    }
}
