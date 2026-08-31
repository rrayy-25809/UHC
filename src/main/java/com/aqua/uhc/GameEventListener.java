package com.aqua.uhc;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;

public class GameEventListener implements Listener {
    private final Random random = new Random();
    private final UHC plugin;
    private final World gameWorld;
    private int playerAmountWhenStart;

    public GameEventListener(UHC plugin, World gameWorld) {
        this.plugin = plugin;
        this.gameWorld = gameWorld;
        // 생성 시점에 월드에 플레이어가 없으면 0일 수 있음. UHCCommand에서 teleport 후 refresh 호출
        this.playerAmountWhenStart = gameWorld.getPlayers().size();
    }

    /** 게임 시작 후 실제 인원을 갱신 (teleport 이후 호출) */
    public void refreshInitialCount() {
        int current = gameWorld.getPlayers().size();
        if (current > 0) {
            this.playerAmountWhenStart = current;
        }
    }

    @EventHandler
    public void onPlayerKilled(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        if (!player.getWorld().equals(gameWorld)) return;

        event.deathMessage(null);

        // lazy init: 아직 0이면 현재 월드 인원으로 보정
        if (playerAmountWhenStart == 0) {
            playerAmountWhenStart = Math.max(1, gameWorld.getPlayers().size());
        }

        long living = gameWorld.getPlayers().stream()
                .filter(p -> !p.getUniqueId().equals(player.getUniqueId()))
                .filter(p -> p.getGameMode() == GameMode.SURVIVAL)
                .count();

        // 사망자는 아직 SURVIVAL이므로 living은 사망 전 생존자 -1
        gameWorld.sendMessage(Component.text("플레이어, " + player.getName() + "이 사망하였습니다. (" + living + "/" + playerAmountWhenStart + ")"));

        ItemStack bonusItem = new ItemStack(Material.GOLDEN_APPLE);
        gameWorld.dropItemNaturally(player.getLocation(), bonusItem);

        // PlayerDeathEvent 내에서 동기 GameMode 변경은 클라이언트 데스스크린 desync 유발 -> 1틱 지연
        UHC effectivePlugin = plugin;
        if (effectivePlugin == null) {
            var p = Bukkit.getPluginManager().getPlugin("UHC");
            if (p instanceof UHC uhc) effectivePlugin = uhc;
        }
        if (effectivePlugin != null) {
            Bukkit.getScheduler().runTask(effectivePlugin, () -> {
                if (player.isOnline()) {
                    player.setGameMode(GameMode.SPECTATOR);
                }
            });
        } else {
            // fallback: 즉시 변경 (플러그인 조회 실패 시)
            player.setGameMode(GameMode.SPECTATOR);
        }
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        if (!player.getWorld().equals(gameWorld)) return;
        if (event.getTo() == null || event.getTo().getWorld() == null) return;

        if (event.getTo().getWorld().getEnvironment() == World.Environment.NETHER ||
                event.getTo().getWorld().getEnvironment() == World.Environment.THE_END) {
            event.setCancelled(true);
            player.sendMessage(Component.text("§c네더와 엔더는 금지되어 있습니다!"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (!player.getWorld().equals(gameWorld)) return;
        if (!block.getWorld().equals(gameWorld)) return;

        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (player.getGameMode() != GameMode.SURVIVAL) return;
        if (itemInHand.containsEnchantment(Enchantment.SILK_TOUCH)) return;
        if (itemInHand.getType() == Material.SHEARS) return;

        Material resultMaterial = null;
        float expToDrop = 0;
        int amount = 1;

        switch (block.getType()) {
            case IRON_ORE:
            case DEEPSLATE_IRON_ORE:
                resultMaterial = Material.IRON_INGOT;
                expToDrop = 0.7f;
                break;
            case RAW_IRON_BLOCK:
                resultMaterial = Material.IRON_INGOT;
                expToDrop = 0.7f;
                amount = 9;
                break;
            case GOLD_ORE:
            case DEEPSLATE_GOLD_ORE:
            case NETHER_GOLD_ORE:
                resultMaterial = Material.GOLD_INGOT;
                expToDrop = 1.0f;
                break;
            case RAW_GOLD_BLOCK:
                resultMaterial = Material.GOLD_INGOT;
                expToDrop = 1.0f;
                amount = 9;
                break;
            case COPPER_ORE:
            case DEEPSLATE_COPPER_ORE:
                resultMaterial = Material.COPPER_INGOT;
                expToDrop = 0.7f;
                amount = 2;
                break;
            case OAK_LEAVES:
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
                amount = (random.nextInt(10) == 0) ? 1 : 0;
                break;
            default:
                return;
        }

        if (resultMaterial == null) return;

        // 사과 0개면 드롭 스킵
        if (resultMaterial == Material.APPLE && amount <= 0) return;

        // 적절한 도구 검증 - 맨손/잘못된 티어로 캤을 때 자동제련 방지 (바닐라 드롭 유지)
        if (isOre(block.getType()) && !isPreferredTool(block, itemInHand)) {
            return;
        }

        event.setDropItems(false);

        int fortuneLevel = itemInHand.getEnchantmentLevel(Enchantment.FORTUNE);
        if (fortuneLevel > 0) {
            int r = random.nextInt(fortuneLevel + 2);
            // 기존 로직 유지하되 최소 1 보장
            amount = Math.max(1, r) * amount;
        }

        if (amount <= 0) return;

        ItemStack dropItem = new ItemStack(resultMaterial, amount);
        block.getWorld().dropItemNaturally(block.getLocation(), dropItem);

        if (expToDrop > 0) {
            int exp = calculateExp(expToDrop, amount);
            if (exp > 0) {
                block.getWorld().spawn(block.getLocation(), ExperienceOrb.class).setExperience(exp);
            }
        }
    }

    private boolean isOre(Material type) {
        return switch (type) {
            case IRON_ORE, DEEPSLATE_IRON_ORE, RAW_IRON_BLOCK,
                 GOLD_ORE, DEEPSLATE_GOLD_ORE, RAW_GOLD_BLOCK, NETHER_GOLD_ORE,
                 COPPER_ORE, DEEPSLATE_COPPER_ORE -> true;
            default -> false;
        };
    }

    private boolean isPreferredTool(Block block, ItemStack tool) {
        // Paper API: isPreferredTool 은 해당 도구가 채굴 속도를 높이는지 확인
        // 맨손이나 잘못된 티어는 false
        try {
            return block.isPreferredTool(tool);
        } catch (NoSuchMethodError e) {
            // 구버전 fallback: 최소한 도구 타입이 피캐즈인지 확인
            Material t = tool.getType();
            return t.name().endsWith("_PICKAXE");
        }
    }

    private int calculateExp(float expPerOne, int amount) {
        float total = expPerOne * amount;
        int base = (int) Math.floor(total);
        float fraction = total - base;
        if (fraction > 0 && random.nextFloat() < fraction) {
            base++;
        }
        return base;
    }
}
