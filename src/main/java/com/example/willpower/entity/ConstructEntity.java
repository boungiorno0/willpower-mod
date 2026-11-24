package com.example.willpower.entity;

import com.example.willpower.core.LanternCorps;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.util.List;
import java.util.UUID;

public class ConstructEntity extends Entity {
    public enum Kind {
        BLAST, FIST, BOOMERANG, CHAIN_BOLT, LIGHT_ORB
    }

    private static final TrackedData<Integer> KIND_ID = DataTracker.registerData(ConstructEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> COLOR_ARGB = DataTracker.registerData(ConstructEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Float> SCALE = DataTracker.registerData(ConstructEntity.class, TrackedDataHandlerRegistry.FLOAT);

    private UUID ownerUuid;
    private int maxLifetime = 60;
    private int bouncesLeft = 3;

    public ConstructEntity(EntityType<?> type, World world) {
        super(type, world);
        this.noClip = true;
    }

    public void configure(PlayerEntity owner, Kind kind, LanternCorps corps, int argbColor, float scale) {
        this.ownerUuid = owner.getUuid();
        this.dataTracker.set(KIND_ID, kind.ordinal());
        this.dataTracker.set(COLOR_ARGB, argbColor);
        this.dataTracker.set(SCALE, scale);
        this.maxLifetime = switch (kind) {
            case LIGHT_ORB -> 600;
            case BOOMERANG -> 80;
            case FIST -> 30;
            case CHAIN_BOLT -> 45;
            case BLAST -> 50;
        };
        Vec3d look = owner.getRotationVec(1.0f);
        double speed = switch (kind) {
            case LIGHT_ORB -> 0.35;
            case FIST -> 1.35;
            case BOOMERANG -> 1.15;
            case CHAIN_BOLT -> 1.85;
            case BLAST -> 1.45;
        };
        this.setPosition(owner.getEyePos().add(look.multiply(0.9)));
        this.setVelocity(look.multiply(speed));
        this.setYaw(owner.getYaw());
        this.setPitch(owner.getPitch());
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(KIND_ID, 0);
        builder.add(COLOR_ARGB, 0xFF00FF44);
        builder.add(SCALE, 1.0f);
    }

    public Kind getKind() {
        int ord = this.dataTracker.get(KIND_ID);
        Kind[] values = Kind.values();
        return (ord >= 0 && ord < values.length) ? values[ord] : Kind.BLAST;
    }

    public int getColorArgb() {
        return this.dataTracker.get(COLOR_ARGB);
    }

    public float getScale() {
        return this.dataTracker.get(SCALE);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.age > maxLifetime) {
            this.discard();
            return;
        }

        Kind kind = getKind();
        if (kind == Kind.LIGHT_ORB) {
            this.setVelocity(this.getVelocity().multiply(0.86));
            this.move(MovementType.SELF, this.getVelocity());
            spawnColoredDust(1);
            return;
        }

        if (kind == Kind.BOOMERANG && this.age > 16 && ownerUuid != null) {
            PlayerEntity owner = getWorld().getPlayerByUuid(ownerUuid);
            if (owner != null) {
                Vec3d back = owner.getEyePos().subtract(this.getPos());
                if (back.lengthSquared() < 1.5) {
                    this.discard();
                    return;
                }
                this.setVelocity(back.normalize().multiply(1.2));
            }
        }

        this.move(MovementType.SELF, this.getVelocity());
        spawnColoredDust(2);

        if (!getWorld().isClient) {
            checkHits(kind);
        }
    }

    private void checkHits(Kind kind) {
        float radius = 0.55f * getScale();
        Box hitBox = this.getBoundingBox().expand(radius);
        List<Entity> targets = getWorld().getOtherEntities(this, hitBox,
                e -> e instanceof LivingEntity && !e.getUuid().equals(ownerUuid) && e.isAlive());

        if (targets.isEmpty()) {
            if (!getWorld().isSpaceEmpty(this.getBoundingBox().contract(0.1)) && kind != Kind.BOOMERANG) {
                if (kind == Kind.BLAST) {
                    getWorld().createExplosion(this, getX(), getY(), getZ(), 1.2f * getScale(), World.ExplosionSourceType.NONE);
                }
                this.discard();
            }
            return;
        }

        LivingEntity hit = (LivingEntity) targets.getFirst();
        PlayerEntity owner = ownerUuid != null ? getWorld().getPlayerByUuid(ownerUuid) : null;
        float dmg = switch (kind) {
            case FIST -> 11.0f * getScale();
            case BLAST -> 6.5f * getScale();
            case BOOMERANG -> 7.0f;
            case CHAIN_BOLT -> 6.0f;
            case LIGHT_ORB -> 0.0f;
        };

        if (dmg > 0) {
            hit.damage(owner != null ? getDamageSources().playerAttack(owner) : getDamageSources().magic(), dmg);
            if (kind == Kind.FIST) {
                hit.addVelocity(this.getVelocity().x * 1.4, 0.55, this.getVelocity().z * 1.4);
                hit.velocityModified = true;
            }
            getWorld().playSound(null, hit.getBlockPos(), SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK,
                    SoundCategory.PLAYERS, 0.9f, 1.3f);
        }

        if (kind == Kind.CHAIN_BOLT && bouncesLeft > 0) {
            bouncesLeft--;
            List<LivingEntity> nextList = getWorld().getEntitiesByClass(LivingEntity.class,
                    hit.getBoundingBox().expand(8.0),
                    e -> e != hit && !e.getUuid().equals(ownerUuid) && e.isAlive());
            if (!nextList.isEmpty()) {
                Vec3d dir = nextList.getFirst().getEyePos().subtract(this.getPos()).normalize();
                this.setVelocity(dir.multiply(1.75));
                return;
            }
        }

        if (kind != Kind.BOOMERANG) {
            if (kind == Kind.BLAST) {
                getWorld().createExplosion(this, getX(), getY(), getZ(), 1.1f * getScale(), World.ExplosionSourceType.NONE);
            }
            this.discard();
        }
    }

    private void spawnColoredDust(int count) {
        if (!(getWorld() instanceof ServerWorld sw)) return;
        int argb = getColorArgb();
        float r = ((argb >> 16) & 0xFF) / 255.0f;
        float g = ((argb >> 8) & 0xFF) / 255.0f;
        float b = (argb & 0xFF) / 255.0f;
        sw.spawnParticles(new DustParticleEffect(new Vector3f(r, g, b), 1.1f * getScale()),
                getX(), getY(), getZ(), count, 0.15, 0.15, 0.15, 0.01);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.containsUuid("Owner")) ownerUuid = nbt.getUuid("Owner");
        this.dataTracker.set(KIND_ID, nbt.getInt("Kind"));
        this.dataTracker.set(COLOR_ARGB, nbt.getInt("Color"));
        this.dataTracker.set(SCALE, nbt.contains("Scale") ? nbt.getFloat("Scale") : 1.0f);
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if (ownerUuid != null) nbt.putUuid("Owner", ownerUuid);
        nbt.putInt("Kind", this.dataTracker.get(KIND_ID));
        nbt.putInt("Color", this.dataTracker.get(COLOR_ARGB));
        nbt.putFloat("Scale", this.dataTracker.get(SCALE));
    }
}
