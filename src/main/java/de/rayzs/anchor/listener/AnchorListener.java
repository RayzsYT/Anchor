package de.rayzs.anchor.listener;

import de.rayzs.anchor.session.AnchorManager;
import de.rayzs.anchor.utils.ExpireCache;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.concurrent.TimeUnit;

public class AnchorListener implements Listener {

    private final ExpireCache<Player, BlockFace> playerBlockFaceMap = new ExpireCache<>(250, TimeUnit.MILLISECONDS);
    private final AnchorManager manager;

    public AnchorListener(final AnchorManager manager) {
        this.manager = manager;
    }



    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final BlockFace face = event.getBlockFace();


        if (player.isOnGround() || player.isFlying()) {
            return;
        }


        if (event.getItem() == null || !event.getItem().getType().name().contains("PICKAXE")) {
            return;
        }


        this.playerBlockFaceMap.putIgnoreIfContains(player, face);
    }


    @EventHandler
    public void onMove(final PlayerMoveEvent event) {
        final Player player = event.getPlayer();


        if (manager.hasSession(player)) {
            final Location from = event.getFrom();
            final Location to = event.getTo();

            final double diffX = Math.abs(from.getX() - to.getX());
            final double diffZ = Math.abs(from.getZ() - to.getZ());


            if (diffX >= 0.05 || diffZ >= 0.05) {
                manager.remove(player, session -> {});
                return;
            }
        }


        final BlockFace hookedBlockFace = playerBlockFaceMap.get(player);
        if (!manager.hasSession(player) && hookedBlockFace == null) {
            return;
        }


        if (player.isOnGround() || player.isFlying()) {
            return;
        }


        {
            final ItemStack holdingItem = player.getInventory().getItemInMainHand();
            if (!holdingItem.getType().name().contains("PICKAXE")) {
                return;
            }
        }


        final Location loc = player.getLocation();
        final Vector dir = loc.getDirection();
        if (Math.abs(dir.getY()) > 0.5) {
            return;
        }


        {
            final Block blockBelow = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
            final Block blockEventMoreBelow = blockBelow.getRelative(BlockFace.DOWN);

            if (blockBelow.getType().isSolid() || blockEventMoreBelow.getType().isSolid()) {
                this.manager.remove(player, session -> {});
                return;
            }
        }


        final RayTraceResult hit = player.getWorld().rayTraceBlocks(
                player.getEyeLocation(),
                dir,
                1.5,
                FluidCollisionMode.NEVER,
                true
        );

        if (hit == null || hit.getHitBlock() == null) {
            return;
        }


        final Block hitBlock = hit.getHitBlock();
        if (!hitBlock.getType().isSolid()) {
            return;
        }


        final double dist = calcXZDistance(
                event.getTo(),
                hitBlock.getLocation().clone().add(0.5, 0, 0.5)
        );

        if (dist >= 0.9) {
            return;
        }


        this.manager.handle(player, hitBlock, hookedBlockFace);
    }

    @EventHandler
    public void onChangeSlot(final PlayerItemHeldEvent event) {
        this.manager.remove(event.getPlayer(), session -> {});
    }

    @EventHandler
    public void onSneak(final PlayerToggleSneakEvent event) {
        final Player player = event.getPlayer();

        if (event.isSneaking()) {
            this.manager.remove(event.getPlayer(), session -> {
                if (session.getCurrentlyHookedBlock() == null) {
                    return;
                }

                final BlockFace hookedBlockFace = session.getHookedBlockFace();
                final Vector vector = hookedBlockFace.getDirection().multiply(0.8).setY(0.3);

                player.setVelocity(vector);


                player.playSound(
                        player.getLocation(),
                        Sound.ENTITY_RABBIT_JUMP,
                        0.6f, 0.2f
                );
            });
        }
    }

    private double calcXZDistance(final Location loc1, final Location loc2) {
        final double diffX = loc1.getX() - loc2.getX();
        final double diffZ = loc1.getZ() - loc2.getZ();

        return Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffZ, 2));
    }
}
