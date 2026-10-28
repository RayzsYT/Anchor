package de.rayzs.anchor.session;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;
import java.util.logging.Logger;

public class AnchorSettings {

    public enum ModeType { TYPE, KEY }


    private final NamespacedKey identifierKey = new NamespacedKey("anchor", "identifier");
    private final NamespacedKey jumpMultiplierKey = new NamespacedKey("anchor", "jump-multiplier");


    private ModeType mode;
    private HashSet<String> typeList;


    public void load(final FileConfiguration config, final Logger logger) {
        final String modeStr = config.getString("mode");

        {
            boolean found = false;
            for (ModeType modeType : ModeType.values()) {
                if (modeType.name().equalsIgnoreCase(modeStr)) {
                    this.mode = modeType;
                    found = true;
                    break;
                }
            }

            if (!found) {
                logger.warning("Invalid mode type \"" + modeStr + "\"! (Available: TYPE, KEY)");
                logger.warning("Mode has been set to TYPE by default to avoid issues.");

                this.mode = ModeType.TYPE;
            }
        }

        this.typeList = new HashSet<>(config.getStringList("types"));
    }


    // Logic methods
    public boolean isValidItem(final ItemStack item) {
        if (this.mode == ModeType.TYPE) {
            return item != null && this.typeList.contains(item.getType().name());
        }


        if (item == null || !item.hasItemMeta()) return false;

        final PersistentDataContainer dataContainer = item.getItemMeta().getPersistentDataContainer();
        return dataContainer.has(this.identifierKey);
    }

    public float readJumpMultiplier(final ItemStack item) {
        if (!isValidItem(item)) return -999;

        final PersistentDataContainer dataContainer = item.getItemMeta().getPersistentDataContainer();
        return dataContainer.getOrDefault(this.jumpMultiplierKey, PersistentDataType.FLOAT, 0.8f);
    }

    public float readJumpHeight(final ItemStack item) {
        if (!isValidItem(item)) return -999;

        final PersistentDataContainer dataContainer = item.getItemMeta().getPersistentDataContainer();
        return dataContainer.getOrDefault(this.jumpMultiplierKey, PersistentDataType.FLOAT, 0.3f);
    }
}
