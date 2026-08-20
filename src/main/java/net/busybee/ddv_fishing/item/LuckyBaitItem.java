package net.busybee.ddv_fishing.item;

import net.busybee.ddv_fishing.registry.ModAttachments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * The 75% "Master Angler" milestone reward. Single use: sets a flag that forces the player's next
 * catch to resolve as a Perfect Catch, then consumes itself. The flag lives on
 * {@link ModAttachments#GUARANTEED_PERFECT} rather than a local field so it survives a relog
 * between using the bait and actually landing the next fish.
 */
public class LuckyBaitItem extends Item {
    public LuckyBaitItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient() && user instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.setAttached(ModAttachments.GUARANTEED_PERFECT, true);
            user.getStackInHand(hand).decrement(1);
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.6f, 1.4f);
        }
        return ActionResult.SUCCESS;
    }
}
