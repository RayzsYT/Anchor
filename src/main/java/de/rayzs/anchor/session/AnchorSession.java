package de.rayzs.anchor.session;

import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;

public class AnchorSession {

    private final Player player;

    private final BlockFace hookedBlockFace;
    private final double defaultGravity;

    private final HashSet<Block> pastBlocks = new HashSet<>();

    private Block currentlyHookedBlock;
    private double sessionGravity;


    public AnchorSession(
            final Player player,
            final BlockFace hookedBlockFace
    ) {
        final AttributeInstance attributeInstance = player.getAttribute(Attribute.GRAVITY);

        this.player = player;
        this.hookedBlockFace = hookedBlockFace;
        this.defaultGravity = attributeInstance != null
                ? attributeInstance.getDefaultValue()
                : 0.08f;

        this.sessionGravity = defaultGravity;

        player.setVelocity(new Vector(0, 0, 0));
    }


    public void handle(final Block block) {
        final AttributeInstance attributeInstance = this.player.getAttribute(Attribute.GRAVITY);

        if (attributeInstance == null) {
            return;
        }


        if (this.currentlyHookedBlock != block && this.sessionGravity != 0) {
            if (this.currentlyHookedBlock != null) {
                pastBlocks.add(this.currentlyHookedBlock);
            }


            this.currentlyHookedBlock = block;
            final float reduceNum = 0.045f;

            this.sessionGravity = Math.max(0, this.sessionGravity - reduceNum);
            attributeInstance.setBaseValue(this.sessionGravity);


            player.getWorld().playSound(
                    player.getLocation(),
                    block.getBlockData().getSoundGroup().getHitSound(),
                    0.6f, 1
            );

            player.getWorld().playEffect(
                    block.getLocation(),
                    Effect.STEP_SOUND,
                    block.getType()
            );
        }

        player.sendBlockDamage(block.getLocation(), pastBlocks.size() * 0.1f + 0.3f);
    }


    public void destroy() {
        final AttributeInstance attributeInstance = this.player.getAttribute(Attribute.GRAVITY);

        if (attributeInstance == null) {
            return;
        }

        attributeInstance.setBaseValue(this.defaultGravity);
    }


    public BlockFace getHookedBlockFace() {
        return this.hookedBlockFace;
    }

    public Block getCurrentlyHookedBlock() {
        return this.currentlyHookedBlock;
    }

    public Player getPlayer() {
        return this.player;
    }
}
