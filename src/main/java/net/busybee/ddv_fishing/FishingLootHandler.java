package net.busybee.ddv_fishing;

import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
//? if >1.21.1 {
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.loot.context.LootContextParameters;
//?} else {
/*import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
*///?}
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.busybee.ddv_fishing.world.FishBiome;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.List;
import java.util.ArrayList;

public class FishingLootHandler {

    private static final TagKey<Item> C_FISHING_RODS = TagKey.of(RegistryKeys.ITEM, Identifier.of("c", "fishing_rods"));
    private static final TagKey<Item> FABRIC_FISHING_RODS = TagKey.of(RegistryKeys.ITEM, Identifier.of("fabric", "fishing_rods"));

    public static List<ItemStack> generateLoot(ServerPlayerEntity player, FishingBobberEntity bobber, int rarity) {
        //? if >1.21.8 {
        ServerWorld world = (ServerWorld) bobber.getEntityWorld();
        //?} else {
        /*ServerWorld world = (ServerWorld) bobber.getWorld();
        *///?}

        String rarityName = switch (rarity) {
            case 1 -> "rare";
            case 2 -> "epic";
            default -> "common";
        };

        // Biome-flavoured tables (ocean/swamp/jungle_<rarity>) exist alongside the generic ones;
        // anywhere else falls back to the plain vanilla-heavy table rather than needing a table
        // for every biome in the game.
        FishBiome biome = FishBiome.classify(world, BlockPos.ofFloored(bobber.getX(), bobber.getY(), bobber.getZ()));
        String biomePrefix = switch (biome) {
            case OCEAN -> "ocean_";
            case SWAMP -> "swamp_";
            case JUNGLE -> "jungle_";
            case OTHER -> "";
        };
        Identifier lootTableId = Identifier.of("ddv_fishing", "gameplay/fishing/" + biomePrefix + rarityName);

        //? if >1.21.1 {
        LootWorldContext parameterSet = new LootWorldContext.Builder(world)
                .add(LootContextParameters.ORIGIN, new Vec3d(bobber.getX(), bobber.getY(), bobber.getZ()))
                .add(LootContextParameters.TOOL, getFishingRod(player))
                .add(LootContextParameters.THIS_ENTITY, bobber)
                .luck(player.getLuck())
                .build(LootContextTypes.FISHING);
        //?} else {
        /*LootContextParameterSet parameterSet = new LootContextParameterSet.Builder(world)
                .add(LootContextParameters.ORIGIN, new Vec3d(bobber.getX(), bobber.getY(), bobber.getZ()))
                .add(LootContextParameters.TOOL, getFishingRod(player))
                .add(LootContextParameters.THIS_ENTITY, bobber)
                .luck(player.getLuck())
                .build(LootContextTypes.FISHING);
        *///?}

        LootTable lootTable = world.getServer().getReloadableRegistries().getLootTable(RegistryKey.of(RegistryKeys.LOOT_TABLE, lootTableId));
        
        //? if >1.21.1 {
        List<ItemStack> loot = lootTable.generateLoot(parameterSet);
        //?} else {
        /*List<ItemStack> loot = lootTable.generateLoot(parameterSet);
        *///?}

        if (loot.isEmpty()) {
            List<ItemStack> fallback = new ArrayList<>();
            fallback.add(new ItemStack(Items.COD));
            return fallback;
        }
        return loot;
    }

    public static void catchFish(ServerPlayerEntity player, FishingBobberEntity bobber, List<ItemStack> lootItems, boolean isPerfect) {
        //? if >1.21.8 {
        ServerWorld world = (ServerWorld) bobber.getEntityWorld();
        //?} else {
        /*ServerWorld world = (ServerWorld) bobber.getWorld();
        *///?}

        boolean hasSpecial = false;
        for (ItemStack stack : lootItems) {
            if (isSpecialItem(stack)) {
                hasSpecial = true;
            }
            ItemStack toGive = stack.copy();
            if (isPerfect && !isSpecialItem(stack)) {
                toGive.setCount(toGive.getCount() * 2);
            }
            if (!player.getInventory().insertStack(toGive)) {
                player.dropItem(toGive, false);
            }
        }

        int xp = world.random.nextBetween(1, 6);
        if (isPerfect) {
            xp += 5;
            if (hasSpecial) {
                xp += 20;
            }

            // One roll, two possible prizes on top of the guaranteed double loot and XP: a bigger
            // XP top-up most of the time, or - rarer - a taste of Luck for the player to carry into
            // their next few casts. Mutually exclusive so a single perfect catch doesn't stack both.
            float bonusRoll = world.random.nextFloat();
            if (bonusRoll < 0.15f) {
                xp += world.random.nextBetween(10, 15);
            } else if (bonusRoll < 0.25f) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 30 * 20, 0));
            }
        }
        if (xp > 0) {
            ExperienceOrbEntity.spawn(world, new Vec3d(player.getX(), player.getY(), player.getZ()), xp);
        }

        if (isPerfect) {
            world.spawnParticles(ParticleTypes.SPLASH, bobber.getX(), bobber.getY(), bobber.getZ(), 20, 0.2, 0.2, 0.2, 0.1);
            world.spawnParticles(ParticleTypes.BUBBLE, bobber.getX(), bobber.getY(), bobber.getZ(), 10, 0.2, 0.2, 0.2, 0.1);
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.5f, 1.5f);
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, 2.0F);
        
        damageFishingRod(player, 1);
        bobber.discard();
    }

    public static void damageFishingRod(ServerPlayerEntity player, int amount) {
        ItemStack mainHand = player.getMainHandStack();
        ItemStack offHand = player.getOffHandStack();

        if (isFishingRod(mainHand)) {
            mainHand.damage(amount, player, EquipmentSlot.MAINHAND);
        } else if (isFishingRod(offHand)) {
            offHand.damage(amount, player, EquipmentSlot.OFFHAND);
        }
    }

    private static ItemStack getFishingRod(ServerPlayerEntity player) {
        ItemStack mainHand = player.getMainHandStack();
        if (isFishingRod(mainHand)) return mainHand;
        ItemStack offHand = player.getOffHandStack();
        if (isFishingRod(offHand)) return offHand;
        return mainHand;
    }

    public static boolean isSpecialItem(ItemStack stack) {
        return stack.isOf(Items.BOW) || stack.isOf(Items.ENCHANTED_BOOK) || isFishingRod(stack);
    }

    /**
     * Whether this stack is any kind of fishing rod - vanilla, Dreamlight, or another mod's.
     * <p>
     * The minigame used to be gated on the Dreamlight rod alone. It is deliberately broad now:
     * a plain vanilla rod fishes the ripples, so the feature does not depend on the custom rod
     * working. Also used to classify <i>loot</i>, where a rod pulled out of the water counts as
     * a rare catch.
     */
    public static boolean isFishingRod(ItemStack stack) {
        return stack.isOf(Items.FISHING_ROD) || stack.getItem() instanceof FishingRodItem
                || stack.isIn(C_FISHING_RODS) || stack.isIn(FABRIC_FISHING_RODS);
    }
}
