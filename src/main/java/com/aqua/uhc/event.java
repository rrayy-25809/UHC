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
        if (itemInHand.getType() == Material.SHEARS) return;

        Material resultMaterial = null;
        float expToDrop = 0;
        int amount = 1;

        // 2. 광물 종류 판정
        switch (block.getType()) {
            case IRON_ORE:
            case DEEPSLATE_IRON_ORE:
            case RAW_IRON_BLOCK:
                resultMaterial = Material.IRON_INGOT;
                expToDrop = 0.7f;
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
                expToDrop = 0.7f;
                amount = 2;
                break;
            case OAK_LEAVES: //사과 드랍
            case SPRUCE_LEAVES:
            case BIRCH_LEAVES:
            case JUNGLE_LEAVES:
            case ACACIA_LEAVES:
            case DARK_OAK_LEAVES:
            case MANGROVE_LEAVES:
            case CHERRY_LEAVES:
            case PALE_OAK_LEAVES:
            case AZALEA_LEAVES:
            case FLOWERING_AZALEA_LEAVES:
                resultMaterial = Material.APPLE;
                amount = (random.nextInt(10) < 1)? 1 : 0; // 1/10 확률
                break;
            default:
                return;
        }
        if (resultMaterial != null) {
            event.setDropItems(false); // 기존 아이템 드롭 방지

            // 3. 행운(Fortune) 로직 계산
            int fortuneLevel = itemInHand.getEnchantmentLevel(Enchantment.FORTUNE);

                if (fortuneLevel > 0 && block.getType() != Material.ANCIENT_DEBRIS) {
                    int r = random.nextInt(fortuneLevel + 2);
                    amount = Math.max(1, r)*amount;
                }

            // 4. 아이템 및 경험치 드롭
            ItemStack dropItem = new ItemStack(resultMaterial, (int) (amount));
            block.getWorld().dropItemNaturally(block.getLocation(), dropItem);

            if (expToDrop > 0) {
                block.getWorld().spawn(block.getLocation(), ExperienceOrb.class).setExperience((int) (expToDrop * amount));
            }
        }
    }
}