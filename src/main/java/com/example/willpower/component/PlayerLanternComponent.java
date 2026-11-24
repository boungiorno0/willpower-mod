package com.example.willpower.component;

import com.example.willpower.core.LanternCorps;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.EnumMap;
import java.util.Map;

public class PlayerLanternComponent implements AutoSyncedComponent, ServerTickingComponent {
    private static final long DESYNC_INTERVAL_TICKS = 336_000L; // 14 игровых дней

    private final PlayerEntity player;

    private LanternCorps activeCorps = null;
    private int rank = 1; // 1..4
    private int charge = 0;
    private long lastSyncTick = 0L;
    private boolean desynced = false;

    // Очки эмоций для каждого корпуса (прокачка и выбор кольца)
    private final Map<LanternCorps, Integer> emotionPoints = new EnumMap<>(LanternCorps.class);

    // Визуальные настройки (как в референсе)
    private boolean suitActive = false;
    private boolean maskActive = true;
    private boolean auraActive = false;
    private boolean insigniaActive = false;
    private int suitStyle = 0;       // 0 = Comic, 1 = Show/Armored
    private int maskHeight = 0;      // -4..+4 пикселя
    private float brightness = 0.0f; // -0.5..+0.5
    private float saturation = 0.0f; // -0.5..+0.5
    private float insigniaDistance = 0.25f;
    private long glowingEyesMask = 0L;
    private boolean eyesCorpsColor = true;
    private float cruiseSpeed = 24.0f;
    private int activePose = 0;      // 0 = выкл, 1..3 = позы

    public PlayerLanternComponent(PlayerEntity player) {
        this.player = player;
        for (LanternCorps c : LanternCorps.values()) {
            emotionPoints.put(c, 0);
        }
    }

    @Override
    public void serverTick() {
        if (activeCorps == null) return;

        long worldTime = player.getWorld().getTime();
        if (!desynced && lastSyncTick > 0 && (worldTime - lastSyncTick) >= DESYNC_INTERVAL_TICKS) {
            desynced = true;
            sync();
        }

        // Раз в секунду: синергии и пассивные ауры корпусов
        if (suitActive && charge > 0 && player.age % 20 == 0) {
            applyCorpsPassives();
        }
    }

    private void applyCorpsPassives() {
        if (activeCorps == LanternCorps.BLUE) {
            // Синий Фонарь лечит союзников рядом и разряжает Жёлтых
            player.getWorld().getPlayers().stream()
                    .filter(p -> p.squaredDistanceTo(player) <= 256.0)
                    .forEach(other -> {
                        PlayerLanternComponent otherData = ModComponents.LANTERN.get(other);
                        if (otherData.getActiveCorps() == LanternCorps.GREEN || other == player) {
                            other.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 40, 0, true, false));
                        } else if (otherData.getActiveCorps() == LanternCorps.YELLOW && otherData.getCharge() > 0) {
                            otherData.drainCharge(15);
                        }
                    });
        } else if (activeCorps == LanternCorps.GREEN && rank >= 2) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 40, 0, true, false));
        } else if (activeCorps == LanternCorps.RED) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 40, 0, true, false));
        }
    }

    public int getMaxCharge() {
        return 5000 * Math.max(1, rank);
    }

    public boolean consumeCharge(int amount) {
        int cost = desynced ? Math.round(amount * 1.6f) : amount;
        if (charge < cost) return false;
        charge -= cost;
        sync();
        return true;
    }

    public void drainCharge(int amount) {
        this.charge = Math.max(0, this.charge - amount);
        sync();
    }

    public void recharge(int amount, boolean resetSync) {
        this.charge = Math.min(getMaxCharge(), this.charge + amount);
        if (resetSync) {
            this.lastSyncTick = player.getWorld().getTime();
            this.desynced = false;
        }
        sync();
    }

    public void activateCorps(LanternCorps corps) {
        this.activeCorps = corps;
        this.charge = getMaxCharge();
        this.lastSyncTick = player.getWorld().getTime();
        this.desynced = false;
        sync();
    }

    public void addEmotion(LanternCorps corps, int amount) {
        emotionPoints.put(corps, Math.min(1000, emotionPoints.getOrDefault(corps, 0) + amount));
        sync();
    }

    public int getEmotion(LanternCorps corps) {
        return emotionPoints.getOrDefault(corps, 0);
    }

    public void sync() {
        ModComponents.LANTERN.sync(this.player);
    }

    // --- Геттеры и сеттеры ---

    public LanternCorps getActiveCorps() { return activeCorps; }
    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = Math.clamp(rank, 1, 4); sync(); }
    public int getCharge() { return charge; }
    public boolean isDesynced() { return desynced; }

    public boolean isSuitActive() { return suitActive; }
    public void setSuitActive(boolean suitActive) { this.suitActive = suitActive; sync(); }

    public boolean isMaskActive() { return maskActive; }
    public void setMaskActive(boolean maskActive) { this.maskActive = maskActive; sync(); }

    public boolean isAuraActive() { return auraActive; }
    public void setAuraActive(boolean auraActive) { this.auraActive = auraActive; sync(); }

    public boolean isInsigniaActive() { return insigniaActive; }
    public void setInsigniaActive(boolean insigniaActive) { this.insigniaActive = insigniaActive; sync(); }

    public int getSuitStyle() { return suitStyle; }
    public void setSuitStyle(int suitStyle) { this.suitStyle = suitStyle; sync(); }

    public int getMaskHeight() { return maskHeight; }
    public void setMaskHeight(int maskHeight) { this.maskHeight = Math.clamp(maskHeight, -4, 4); sync(); }

    public float getBrightness() { return brightness; }
    public void setBrightness(float brightness) { this.brightness = Math.clamp(brightness, -0.5f, 0.5f); sync(); }

    public float getSaturation() { return saturation; }
    public void setSaturation(float saturation) { this.saturation = Math.clamp(saturation, -0.5f, 0.5f); sync(); }

    public float getInsigniaDistance() { return insigniaDistance; }
    public void setInsigniaDistance(float dist) { this.insigniaDistance = Math.clamp(dist, 0.05f, 0.55f); sync(); }

    public long getGlowingEyesMask() { return glowingEyesMask; }
    public void setGlowingEyesMask(long mask) { this.glowingEyesMask = mask; sync(); }

    public boolean isEyesCorpsColor() { return eyesCorpsColor; }
    public void setEyesCorpsColor(boolean eyesCorpsColor) { this.eyesCorpsColor = eyesCorpsColor; sync(); }

    public float getCruiseSpeed() { return cruiseSpeed; }
    public void setCruiseSpeed(float cruiseSpeed) { this.cruiseSpeed = Math.clamp(cruiseSpeed, 8.0f, 64.0f); sync(); }

    public int getActivePose() { return activePose; }
    public void setActivePose(int activePose) { this.activePose = activePose; sync(); }

    public int getDisplayArgb() {
        return activeCorps == null ? 0xFFFFFFFF : activeCorps.getTintedArgb(brightness, saturation);
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.activeCorps = tag.contains("Corps") ? LanternCorps.byId(tag.getString("Corps")) : null;
        this.rank = tag.contains("Rank") ? Math.clamp(tag.getInt("Rank"), 1, 4) : 1;
        this.charge = tag.getInt("Charge");
        this.lastSyncTick = tag.getLong("LastSync");
        this.desynced = tag.getBoolean("Desynced");

        NbtCompound emotions = tag.getCompound("Emotions");
        for (LanternCorps c : LanternCorps.values()) {
            emotionPoints.put(c, emotions.getInt(c.getId()));
        }

        this.suitActive = tag.getBoolean("SuitActive");
        this.maskActive = !tag.contains("MaskActive") || tag.getBoolean("MaskActive");
        this.auraActive = tag.getBoolean("AuraActive");
        this.insigniaActive = tag.getBoolean("InsigniaActive");
        this.suitStyle = tag.getInt("SuitStyle");
        this.maskHeight = tag.getInt("MaskHeight");
        this.brightness = tag.getFloat("Brightness");
        this.saturation = tag.getFloat("Saturation");
        this.insigniaDistance = tag.contains("InsigniaDist") ? tag.getFloat("InsigniaDist") : 0.25f;
        this.glowingEyesMask = tag.getLong("EyesMask");
        this.eyesCorpsColor = !tag.contains("EyesCorps") || tag.getBoolean("EyesCorps");
        this.cruiseSpeed = tag.contains("CruiseSpeed") ? tag.getFloat("CruiseSpeed") : 24.0f;
        this.activePose = tag.getInt("ActivePose");
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        if (activeCorps != null) tag.putString("Corps", activeCorps.getId());
        tag.putInt("Rank", rank);
        tag.putInt("Charge", charge);
        tag.putLong("LastSync", lastSyncTick);
        tag.putBoolean("Desynced", desynced);

        NbtCompound emotions = new NbtCompound();
        for (LanternCorps c : LanternCorps.values()) {
            emotions.putInt(c.getId(), emotionPoints.getOrDefault(c, 0));
        }
        tag.put("Emotions", emotions);

        tag.putBoolean("SuitActive", suitActive);
        tag.putBoolean("MaskActive", maskActive);
        tag.putBoolean("AuraActive", auraActive);
        tag.putBoolean("InsigniaActive", insigniaActive);
        tag.putInt("SuitStyle", suitStyle);
        tag.putInt("MaskHeight", maskHeight);
        tag.putFloat("Brightness", brightness);
        tag.putFloat("Saturation", saturation);
        tag.putFloat("InsigniaDist", insigniaDistance);
        tag.putLong("EyesMask", glowingEyesMask);
        tag.putBoolean("EyesCorps", eyesCorpsColor);
        tag.putFloat("CruiseSpeed", cruiseSpeed);
        tag.putInt("ActivePose", activePose);
    }
}
