package com.aqua.uhc;

import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRules;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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
        worldManager.createWorld(CreateWorldOptions.worldName("test_game")
                .environment(Environment.NORMAL)
                .worldType(WorldType.NORMAL)
                .generateStructures(true))
        .onFailure(reason -> {
            sender.sendMessage("실패!");
        })
        .onSuccess(world -> {
            World bukkitWorld = world.getBukkitWorld().get();

            world.setPvp(false);
            world.setDifficulty(Difficulty.NORMAL);
            world.setGameMode(GameMode.SURVIVAL);
            bukkitWorld.setGameRule(GameRules.ADVANCE_TIME, false);
            bukkitWorld.setGameRule(GameRules.IMMEDIATE_RESPAWN, true);

            this.plugin.getServer().getPluginManager().registerEvents(new gameEvent(bukkitWorld), this.plugin);
            timeManager timeManager = new timeManager(this.plugin, bukkitWorld, 15*TimeUnit.MINUTE, TimeUnit.HOUR);
            timeManager.run();
        });
        return true;
    }
}
