package de.rayzs.anchor.session;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.HashMap;

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

    public AnchorSession getSession(final Player player) {
        return this.anchors.get(player);
    }

    public AnchorSession remove(final Player player) {
        final AnchorSession session = this.anchors.remove(player);

        if (session != null) {
            session.destroy();
        }

        return session;
    }

    public void destruct() {
        this.anchors.values().forEach(AnchorSession::destroy);
        this.anchors.clear();
    }
}
