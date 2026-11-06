package de.rayzs.anchor.session;

import de.rayzs.anchor.utils.ExpireList;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class AnchorManager {

    private final ExpireList<Player> reducedFallDamagePlayers = new ExpireList<>(1, TimeUnit.MINUTES);
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
            reducedFallDamagePlayers.add(player);
        }

        return session;
    }

    public double handleFallDamage(final Player player, final double fallDamage) {
        if (reducedFallDamagePlayers.remove(player)) {
            return Math.max(0, fallDamage - (fallDamage * 0.8));
        }

        return fallDamage;
    }

    public void destruct() {
        this.anchors.values().forEach(AnchorSession::destroy);
        this.anchors.clear();
    }
}
