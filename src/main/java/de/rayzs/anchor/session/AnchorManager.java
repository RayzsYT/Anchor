package de.rayzs.anchor.session;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.function.Consumer;

public class AnchorManager {

    private final HashMap<Player, AnchorSession> anchors = new HashMap<>();


    public void handle(
            final Player player,
            final Block block,
            final BlockFace hookedBlockFace
    ) {
        AnchorSession session = this.anchors.get(player);

        if (session == null) {
            session = new AnchorSession(player, hookedBlockFace);
            this.anchors.put(player, session);
        }


        session.handle(block);
    }

    public boolean hasSession(final Player player) {
        return this.anchors.get(player) != null;
    }

    public void remove(
            final Player player,
            final Consumer<AnchorSession> removalCallback
    ) {
        final AnchorSession session = this.anchors.remove(player);

        if (session != null) {
            session.destroy();
            removalCallback.accept(session);
        }
    }
}
