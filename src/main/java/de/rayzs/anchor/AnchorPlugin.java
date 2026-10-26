package de.rayzs.anchor;

import de.rayzs.anchor.listener.AnchorListener;
import de.rayzs.anchor.session.AnchorManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AnchorPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        final AnchorManager manager = new AnchorManager();

        getServer().getPluginManager().registerEvents(new AnchorListener(manager), this);
    }

    @Override
    public void onDisable() {

    }
}
