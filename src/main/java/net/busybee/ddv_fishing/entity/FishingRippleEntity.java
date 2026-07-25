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
/*import net.minecraft.nbt.NbtCompound;*/
//?}
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class FishingRippleEntity extends Entity {
    private static final TrackedData<Integer> RARITY = DataTracker.registerData(FishingRippleEntity.class, TrackedDataHandlerRegistry.INTEGER);

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
    }

    public void setRarity(int rarity) {
        this.getDataTracker().set(RARITY, rarity);
    }

    public int getRarity() {
        return this.getDataTracker().get(RARITY);
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
    }*/
    //?}

    @Override
    public void tick() {
        super.tick();
        //? if >1.21.8 {
        World world = this.getEntityWorld();
        //?}
        //? if <=1.21.8 {
        /*World world = this.getWorld();*/
        //?}
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
            int count = switch (getRarity()) {
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
            }

            if (getRarity() > 0) {
                serverWorld.spawnParticles(ParticleTypes.SPLASH, this.getX(), this.getY(), this.getZ(), getRarity(), 0.3, 0.0, 0.3, 0.0);
            }
        }
        return; // Skip old logic
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
        setRarity(nbt.getInt("Rarity").orElse(0));
        //?} else {
        setRarity(nbt.getInt("Rarity"));
        //?}
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("Rarity", getRarity());
    }*/
    //?}
}
