package de.rayzs.anchor.listener;

import de.rayzs.anchor.session.AnchorManager;
import de.rayzs.anchor.session.AnchorSession;
import de.rayzs.anchor.utils.ExpireCache;
import de.rayzs.anchor.utils.ExpireList;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.concurrent.TimeUnit;

public class AnchorListener implements Listener {

    private final ExpireCache<Player, BlockFace> playerBlockFaceMap = new ExpireCache<>(250, TimeUnit.MILLISECONDS);
    private final ExpireList<Player> playerDoubleSneak = new ExpireList<>(250, TimeUnit.MILLISECONDS);


    private final NamespacedKey identifierKey = new NamespacedKey("anchor", "identifier");
    private final NamespacedKey jumpMultiplierKey = new NamespacedKey("anchor", "jump-multiplier");


    private final AnchorManager manager;


    public AnchorListener(final AnchorManager manager) {
        this.manager = manager;
    }



    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();


        if (event.getClickedBlock() == null) {
            return;
        }


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
                manager.remove(player);
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
                this.manager.remove(player);
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
    public void onSneak(final PlayerToggleSneakEvent event) {
        final Player player = event.getPlayer();

        if (!this.manager.hasSession(player)) return;


        if (!player.isSneaking() && !playerDoubleSneak.contains(player)) {
            playerDoubleSneak.add(player);
            return;
        }


        if (player.isSneaking()) return;


        final AnchorSession session = this.manager.remove(event.getPlayer());

        if (session.getCurrentlyHookedBlock() == null) {
            return;
        }

        final Block hookedBlock = session.getCurrentlyHookedBlock();
        final BlockFace hookedBlockFace = session.getHookedBlockFace();
        final Vector vector = hookedBlockFace.getDirection().multiply(0.8).setY(0.3);


        player.getWorld().playEffect(
                player.getLocation(),
                Effect.STEP_SOUND,
                hookedBlock.getType()
        );


        player.playSound(
                player.getLocation(),
                Sound.ENTITY_BAT_TAKEOFF,
                0.2f, 2
        );


        player.setVelocity(vector);
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {
        final Block block = event.getBlock();
        final AnchorSession session = this.manager.getSession(event.getPlayer());

        if (session != null && session.getCurrentlyHookedBlock().equals(block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onChangeSlot(final PlayerItemHeldEvent event) {
        this.manager.remove(event.getPlayer());
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        this.manager.remove(event.getPlayer());
    }

    @EventHandler
    public void onTeleport(final PlayerTeleportEvent event) {
        this.manager.remove(event.getPlayer());
    }

    @EventHandler
    public void onChangeWorld(final PlayerChangedWorldEvent event) {
        this.manager.remove(event.getPlayer());
    }

    @EventHandler
    public void onSwapItems(final PlayerSwapHandItemsEvent event) {
        this.manager.remove(event.getPlayer());
    }



    private boolean isValidItem(final ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        final PersistentDataContainer dataContainer = item.getItemMeta().getPersistentDataContainer();
        return dataContainer.has(identifierKey);
    }

    private float readJumpMultiplier(final ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;

        final PersistentDataContainer dataContainer = item.getItemMeta().getPersistentDataContainer();
        return dataContainer.getOrDefault(jumpMultiplierKey, PersistentDataType.FLOAT, 0.8f);
    }

    private float readJumpHeight(final ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;

        final PersistentDataContainer dataContainer = item.getItemMeta().getPersistentDataContainer();
        return dataContainer.getOrDefault(jumpMultiplierKey, PersistentDataType.FLOAT, 0.3f);
    }

    private double calcXZDistance(final Location loc1, final Location loc2) {
        final double diffX = loc1.getX() - loc2.getX();
        final double diffZ = loc1.getZ() - loc2.getZ();

        return Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffZ, 2));
    }


}
