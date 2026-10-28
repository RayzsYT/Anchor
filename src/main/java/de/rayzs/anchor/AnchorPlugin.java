package de.rayzs.anchor;

import de.rayzs.anchor.listener.AnchorListener;
import de.rayzs.anchor.session.AnchorManager;
import de.rayzs.anchor.session.AnchorSettings;
import org.bukkit.event.HandlerList;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class AnchorPlugin extends JavaPlugin {


    private AnchorManager manager;
    private AnchorSettings settings;


    @Override
    public void onLoad() {
        this.manager = new AnchorManager();
        this.settings = new AnchorSettings();

        saveDefaultConfig();
        reloadConfig();

        this.settings.load(getConfig(), getLogger());
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(
                new AnchorListener(manager, this.settings), this
        );

        getCommand("anchorreload").setExecutor((sender, command, label, args) -> {

            try {
                updateConfig();
                sender.sendMessage("Config reloaded!");
            } catch (final Exception exception) {
                sender.sendMessage("Something went wrong! Check the console for more.");
                exception.printStackTrace();
            }

            return true;
        });
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        this.manager.destruct();
    }

    public void updateConfig() {
        reloadConfig();

        this.settings.load(getConfig(), getLogger());
    }
}
