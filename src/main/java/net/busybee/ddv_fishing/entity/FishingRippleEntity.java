package net.busybee.ddv_fishing.entity;

import net.busybee.ddv_fishing.Ddv_fishing;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
//? if >1.21.5 {
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
//?}
//? if <=1.21.5 {
/*import net.minecraft.nbt.NbtCompound;
*///?}
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * The disturbance on the water that marks a catchable spot.
 * <p>
 * Draws the GeckoLib ring model from {@code geckolib/models/water_ripple.geo.json}, tinted per
 * rarity by the renderer, on top of the particles from {@link #spawnParticles}.
 * <p>
 * The model only works because its texture is generated with binary alpha by
 * {@code tools/gen_ripple_texture.mjs}. Each ring is a flat square plate, so a fully opaque
 * texture renders as a solid slab sitting in the water rather than as rings - which is exactly
 * what two earlier hand-painted textures did. Do not replace that PNG with one lacking real
 * transparency.
 */
public class FishingRippleEntity extends Entity implements GeoEntity {
    /** Ambient state: the ripple is just sitting there waiting to be fished. */
    public static final int STATE_IDLE = 0;
    /** A fish has taken the hook - the water churns harder. */
    public static final int STATE_BITE = 1;
    /** The player is working the reeling minigame. */
    public static final int STATE_REELING = 2;

    private static final TrackedData<Integer> RARITY = DataTracker.registerData(FishingRippleEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> ACTIVITY_STATE = DataTracker.registerData(FishingRippleEntity.class, TrackedDataHandlerRegistry.INTEGER);

    /**
     * The rarity colour, as packed 0xRRGGBB - blue is the easy catch, green middling, orange hard.
     * <p>
     * Single source of truth on purpose. The ring tint and the ripple's own dust particles both
     * read it, because they were previously picked independently and ended up contradicting each
     * other: green rings surrounded by orange particles, and vice versa.
     */
    public static int getRarityRgb(int rarity) {
        return switch (rarity) {
            case 1 -> 0x5FE08A;
            case 2 -> 0xFF9A3C;
            default -> 0x5AB4FF;
        };
    }

    /** Names must match the animations in water_ripple.animation.json. */
    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation ANIM_BITE = RawAnimation.begin().thenPlay("bite").thenLoop("idle");
    private static final RawAnimation ANIM_REELING = RawAnimation.begin().thenLoop("reeling");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int maxAge = 1200;

    public FishingRippleEntity(EntityType<?> type, World world) {
        super(type, world);
        this.noClip = true;
        this.setNoGravity(true);
        this.maxAge = 600 + world.getRandom().nextInt(600);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(RARITY, 0);
        builder.add(ACTIVITY_STATE, STATE_IDLE);
    }

    public void setRarity(int rarity) {
        this.getDataTracker().set(RARITY, rarity);
    }
    public int getRarity() {
        return this.getDataTracker().get(RARITY);
    }
    /** @param state one of {@link #STATE_IDLE}, {@link #STATE_BITE}, {@link #STATE_REELING}. */
    public void setActivityState(int state) {
        this.getDataTracker().set(ACTIVITY_STATE, state);
    }
    public int getActivityState() {
        return this.getDataTracker().get(ACTIVITY_STATE);
    }

    //? if >1.21.1 {
    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }
    //?}
    //? if <=1.21.1 {
    /*@Override
    public boolean damage(DamageSource source, float amount) {
        return false;
    }
    *///?}

    @Override
    public void tick() {
        super.tick();
        //? if >1.21.8 {
        World world = this.getEntityWorld();
        //?}
        //? if <=1.21.8 {
        /*World world = this.getWorld();
        *///?}
        if (world instanceof ServerWorld serverWorld) {
            // Turning the mod off stops new ripples spawning, but existing ones would otherwise
            // sit there churning particles until they aged out - up to a minute of a feature the
            // server owner just disabled.
            if (!Ddv_fishing.CONFIG.enabled || this.age > maxAge) {
                this.discard();
            } else {
                spawnParticles(serverWorld);
            }
        }
    }

    @Override
    public boolean shouldSave() {
        return false;
    }

    /**
     * ACTIVITY_STATE is tracked data, so the value the client animates from is the one the server
     * set - no extra packet needed.
     */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("controller", 4, state -> switch (getActivityState()) {
            case STATE_BITE -> state.setAndContinue(ANIM_BITE);
            case STATE_REELING -> state.setAndContinue(ANIM_REELING);
            default -> state.setAndContinue(ANIM_IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    private void spawnParticles(ServerWorld serverWorld) {
        int state = getActivityState();

        // An idle ripple breathes every other tick. Once a fish is on the hook the water works
        // every tick, and harder again while the player is reeling. Particles are the only
        // feedback the ripple has, so they carry the states on their own.
        if (this.age % (state == STATE_IDLE ? 2 : 1) != 0) return;

        int rarity = getRarity();
        // Lower than it used to be. These counts were set when the particles were the entire
        // ripple; the ring model now carries the shape, so this only has to add motion.
        int count = switch (rarity) {
            case 1 -> 3;
            case 2 -> 4;
            default -> 2;
        } + switch (state) {
            case STATE_BITE -> 3;
            case STATE_REELING -> 6;
            default -> 0;
        };

        var biome = serverWorld.getBiome(this.getBlockPos());
        boolean isJungle = biome.getKey().map(key -> key.getValue().getPath().contains("jungle")).orElse(false);
        boolean isSwamp = biome.getKey().map(key -> key.getValue().getPath().contains("swamp")).orElse(false);
        boolean isOcean = biome.isIn(BiomeTags.IS_OCEAN);

        // A hooked fish pulls the churn in towards the bobber instead of letting it drift wide.
        double spread = state == STATE_IDLE ? 1.1 : 0.7;

        for (int i = 0; i < count; i++) {
            double angle = serverWorld.getRandom().nextDouble() * Math.PI * 2;
            double radius = 0.4 + serverWorld.getRandom().nextDouble() * spread;
            double dx = Math.cos(angle) * radius;
            double dz = Math.sin(angle) * radius;

            if (isOcean) {
                serverWorld.spawnParticles(ParticleTypes.BUBBLE_POP, this.getX() + dx, this.getY() + 0.1, this.getZ() + dz, 1, 0, 0, 0, 0.01);
            } else if (isJungle || isSwamp) {
                serverWorld.spawnParticles(ParticleTypes.SPORE_BLOSSOM_AIR, this.getX() + dx, this.getY() + 0.1, this.getZ() + dz, 1, 0, 0, 0, 0.01);
            }

            serverWorld.spawnParticles(ParticleTypes.BUBBLE, this.getX() + dx, this.getY(), this.getZ() + dz, 1, 0, 0.1, 0, 0.01);

            if (i % 2 == 0) {
                serverWorld.spawnParticles(ParticleTypes.FISHING, this.getX() + dx * 0.7, this.getY(), this.getZ() + dz * 0.7, 1, 0, 0, 0, 0.02);
            }

            // Dust takes an explicit colour, so the accent particles are literally the same RGB as
            // the rings rather than a fixed vanilla particle that happened to be the wrong hue.
            // WAX_ON (orange) and GLOW (green stars) used to do this job and contradicted the
            // tint; GLOW in particular swamped the whole effect.
            if (rarity > 0 && i % 3 == 0) {
                serverWorld.spawnParticles(new DustParticleEffect(getRarityRgb(rarity), 0.8f),
                        this.getX() + dx, this.getY() + 0.1, this.getZ() + dz, 1, 0, 0, 0, 0);
            }
        }

        // Rare ripples always splash; a bite or a reel splashes whatever the rarity, so the
        // player can see the strike land even on a common spot.
        int splash = rarity * 3 + (state == STATE_IDLE ? 0 : 4);
        if (splash > 0) {
            serverWorld.spawnParticles(ParticleTypes.SPLASH, this.getX(), this.getY(), this.getZ(), splash, 0.4, 0.0, 0.4, 0.0);
        }
    }

    //? if >1.21.5 {
    @Override
    protected void readCustomData(ReadView view) {
        setRarity(view.getInt("Rarity", 0));
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.putInt("Rarity", getRarity());
    }
    //?}
    //? if <=1.21.5 {
    /*@Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        //? if =1.21.5 {
        /^setRarity(nbt.getInt("Rarity").orElse(0));
        ^///?} else {
        setRarity(nbt.getInt("Rarity"));
        //?}
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("Rarity", getRarity());
    }
    *///?}
}
