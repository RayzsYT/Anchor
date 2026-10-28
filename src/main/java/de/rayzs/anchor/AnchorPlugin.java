package de.rayzs.anchor;

import de.rayzs.anchor.listener.AnchorListener;
import de.rayzs.anchor.session.AnchorManager;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class AnchorPlugin extends JavaPlugin {


    private AnchorManager manager;


    @Override
    public void onEnable() {
        this.manager = new AnchorManager();

        getServer().getPluginManager().registerEvents(new AnchorListener(manager), this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        this.manager.destruct();
    }
}
