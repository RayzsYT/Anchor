package de.rayzs.anchor.session;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

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
    }


    public void handle(final Block block) {
        final AttributeInstance attributeInstance = this.player.getAttribute(Attribute.GRAVITY);

        if (attributeInstance == null) {
            return;
        }


        if ((this.currentlyHookedBlock == null || !this.currentlyHookedBlock.equals(block)) && this.sessionGravity != 0) {

            if (this.currentlyHookedBlock != null) {
                pastBlocks.add(this.currentlyHookedBlock);
            }


            final float reduceNum = determineHardnessReduction(block.getType());


            this.currentlyHookedBlock = block;
            this.sessionGravity = Math.max(0, this.sessionGravity - reduceNum);

            attributeInstance.setBaseValue(this.sessionGravity);


            player.getWorld().playSound(
                    player.getLocation(),
                    block.getBlockData().getSoundGroup().getStepSound(),
                    0.4f, 1
            );

            player.getWorld().playEffect(
                    player.getLocation(),
                    Effect.STEP_SOUND,
                    block.getType()
            );

            player.setVelocity(player.getVelocity().setY(0));
        }

        player.sendBlockDamage(block.getLocation(), Math.min(pastBlocks.size() * 0.1f + 0.3f, 1));
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


    private float determineHardnessReduction(final Material material) {
        final float hardness = material.getHardness();


        if (hardness == -1 || hardness >= 50) {
            return 1;

        } else if (hardness > 1 && hardness < 5) {
            return (float) sessionGravity * 0.4f;
        }


        return (float) sessionGravity * 0.1f;
    }
}
