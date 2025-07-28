package org.flennn;

import litebans.api.Database;
import litebans.api.Entry;
import litebans.api.Events;
import net.digitalingot.feather.serverapi.api.FeatherAPI;
import net.digitalingot.feather.serverapi.api.model.FeatherMod;
import net.digitalingot.feather.serverapi.api.player.FeatherPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

public final class FeatherUtils extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
    if (!getServer().getPluginManager().isPluginEnabled("LiteBans")) {
            getLogger().severe("LiteBans is not enabled! Disabling FeatherUtils.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!getServer().getPluginManager().isPluginEnabled("FeatherServerAPI")) {
            getLogger().severe("FeatherServerAPI is not enabled! Disabling FeatherUtils.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("FeatherUtils is starting up...");
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("FeatherUtils is enabled!");
        registerEvents();
    }


    @Override
    public void onDisable() {}

    @EventHandler(ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        Player bukkitPlayer = event.getPlayer();
        UUID uuid = bukkitPlayer.getUniqueId();
        String ip = bukkitPlayer.getAddress().getAddress().getHostAddress();

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            boolean muted = Database.get().isPlayerMuted(uuid, ip);

            Bukkit.getScheduler().runTask(this, () -> {
                FeatherPlayer player = FeatherAPI.getPlayerService().getPlayer(uuid);
                if (player == null) return;

                Collection<FeatherMod> mods = Collections.singletonList(new FeatherMod("voice"));
                if (muted) {
                    player.blockMods(mods);
                } else {
                    player.unblockMods(mods);
                }
            });
        });
    }


    public void registerEvents() {
        Events.get().register(new Events.Listener() {
            @Override
            public void entryAdded(Entry entry) {
                if (entry.getType().equals("mute")) {
                    FeatherPlayer player = FeatherAPI.getPlayerService().getPlayer(UUID.fromString(Objects.requireNonNull(entry.getUuid())));
                    Collection<FeatherMod> modsToDisable = Collections.singletonList(new FeatherMod("voice"));
                    assert player != null;
                    player.blockMods(modsToDisable);
                }
            }
            @Override
            public void entryRemoved(Entry entry) {
                if (entry.getType().equals("mute")) {
                    FeatherPlayer player = FeatherAPI.getPlayerService().getPlayer(UUID.fromString(entry.getUuid()));
                    Collection<FeatherMod> modsToEnable = Collections.singletonList(new FeatherMod("voice"));
                    assert player != null;
                    player.unblockMods(modsToEnable);
                }
            }
        });
    }




}
