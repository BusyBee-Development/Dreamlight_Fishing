package net.busybee.ddv_fishing.entity;

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
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
// GeckoLib package layout differs per version:
//   4.6.6 (<=1.21.1):        animation.AnimatableManager        + animation.AnimationController
//   5.3   (1.21.2-1.21.10):  animatable.manager.AnimatableManager + animatable.processing.AnimationController
//   5.4+  (>=1.21.11):       animatable.manager.AnimatableManager + animation.AnimationController
//? if <1.21.2 {
/*import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
*///?} elif <1.21.11 {
/*import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
*///?} else {
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
//?}
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class FishingRippleEntity extends Entity implements GeoEntity {
    private static final TrackedData<Integer> RARITY = DataTracker.registerData(FishingRippleEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> ANIM_STATE = DataTracker.registerData(FishingRippleEntity.class, TrackedDataHandlerRegistry.INTEGER);
    
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
        builder.add(ANIM_STATE, 0);
    }

    public void setRarity(int rarity) {
        this.getDataTracker().set(RARITY, rarity);
    }
    public int getRarity() {
        return this.getDataTracker().get(RARITY);
    }
    public void setAnimState(int state) {
        this.getDataTracker().set(ANIM_STATE, state);
    }
    public int getAnimState() {
        return this.getDataTracker().get(ANIM_STATE);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        //? if <1.21.2 {
        /*
        controllers.add(new AnimationController<>(this, "controller", 5, state -> {
            int s = getAnimState();
            if (s == 2) return state.setAndContinue(RawAnimation.begin().thenLoop("reeling"));
            if (s == 1) return state.setAndContinue(RawAnimation.begin().thenPlay("bite"));
            return state.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
        */
        //?} else {
        controllers.add(new AnimationController<>("controller", 5, state -> {
            int s = getAnimState();
            if (s == 2) return state.setAndContinue(RawAnimation.begin().thenLoop("reeling"));
            if (s == 1) return state.setAndContinue(RawAnimation.begin().thenPlay("bite"));
            return state.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
        //?}
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
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
            if (this.age > maxAge) {
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

    private void spawnParticles(ServerWorld serverWorld) {
        if (this.age % 2 == 0) {
            int rarity = getRarity();
            int count = switch (rarity) {
                case 1 -> 4;
                case 2 -> 7;
                default -> 2;
            };

            var biome = serverWorld.getBiome(this.getBlockPos());
            boolean isJungle = biome.getKey().map(key -> key.getValue().getPath().contains("jungle")).orElse(false);
            boolean isSwamp = biome.getKey().map(key -> key.getValue().getPath().contains("swamp")).orElse(false);
            boolean isOcean = biome.isIn(BiomeTags.IS_OCEAN);

            for (int i = 0; i < count; i++) {
                double angle = serverWorld.getRandom().nextDouble() * Math.PI * 2;
                double radius = 0.4 + serverWorld.getRandom().nextDouble() * 1.1;
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

                if (rarity == 1) {
                    serverWorld.spawnParticles(ParticleTypes.WAX_ON, this.getX() + dx, this.getY() + 0.1, this.getZ() + dz, 1, 0, 0, 0, 0);
                } else if (rarity == 2) {
                    serverWorld.spawnParticles(ParticleTypes.GLOW, this.getX() + dx, this.getY() + 0.1, this.getZ() + dz, 1, 0, 0, 0, 0);
                }
            }

            if (rarity > 0) {
                serverWorld.spawnParticles(ParticleTypes.SPLASH, this.getX(), this.getY(), this.getZ(), rarity * 3, 0.4, 0.0, 0.4, 0.0);
            }
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
