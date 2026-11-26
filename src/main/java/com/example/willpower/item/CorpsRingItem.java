package com.example.willpower.item;

import com.example.willpower.component.ModComponents;
import com.example.willpower.component.PlayerLanternComponent;
import com.example.willpower.core.LanternCorps;
import com.example.willpower.core.SpectrumSynergy;
import com.example.willpower.entity.ConstructEntity;
import com.example.willpower.entity.ModEntities;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketItem;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class CorpsRingItem extends TrinketItem {
    private static final String MODE_KEY = "construct_kind";
    private static final long DEFAULT_EYES_MASK = (1L << 33) | (1L << 34) | (1L << 37) | (1L << 38);

    private final LanternCorps corps;

    public CorpsRingItem(LanternCorps corps, Settings settings) {
        super(settings);
        this.corps = corps;
    }

    public LanternCorps getCorps() {
        return corps;
    }

    public static ConstructEntity.Kind getKind(ItemStack stack) {
        int idx = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getInt(MODE_KEY);
        ConstructEntity.Kind[] all = ConstructEntity.Kind.values();
        return (idx >= 0 && idx < all.length) ? all[idx] : ConstructEntity.Kind.BLAST;
    }

    private static void nextKind(ItemStack stack) {
        ConstructEntity.Kind[] all = ConstructEntity.Kind.values();
        int next = (getKind(stack).ordinal() + 1) % all.length;
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> nbt.putInt(MODE_KEY, next));
    }

    @Override
    public void onEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (!entity.getWorld().isClient && entity instanceof PlayerEntity player) {
            activateNewRing(player, ModComponents.LANTERN.get(player));
        }
    }

    @Override
    public void onUnequip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (!entity.getWorld().isClient && entity instanceof PlayerEntity player) {
            boolean stillHasRing = TrinketsApi.getTrinketComponent(player)
                    .map(c -> c.isEquipped(s -> s.getItem() instanceof CorpsRingItem))
                    .orElse(false);
            if (!stillHasRing) {
                PlayerLanternComponent data = ModComponents.LANTERN.get(player);
                data.setSuitActive(false);
                data.setAuraActive(false);
                data.setInsigniaActive(false);
                data.setGlowingEyesMask(0L);
                updateFlight(player, false);
            }
        }
    }

    @Override
    public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (!entity.getWorld().isClient && entity instanceof PlayerEntity player) {
            handleRingTick(player, entity.getWorld());
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (world.isClient) return TypedActionResult.success(stack, true);

        PlayerLanternComponent data = ModComponents.LANTERN.get(player);

        if (hand == Hand.OFF_HAND) {
            if (data.getActiveCorps() != corps) {
                activateNewRing(player, data);
            } else {
                boolean next = !data.isSuitActive();
                data.setSuitActive(next);
                data.setAuraActive(next);
                data.setInsigniaActive(next);
                data.setGlowingEyesMask(next ? DEFAULT_EYES_MASK : 0L);
                updateFlight(player, next && data.getCharge() > 0);
            }
            world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 0.7f, 1.4f);
            return TypedActionResult.success(stack, false);
        }

        if (player.isSneaking()) {
            nextKind(stack);
            player.sendMessage(Text.literal("Режим: " + getKind(stack).name()).styled(s -> s.withColor(corps.getBaseRgb())), true);
            world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 0.9f, 1.3f);
            return TypedActionResult.success(stack, false);
        }

        if (data.getActiveCorps() != corps) {
            activateNewRing(player, data);
            return TypedActionResult.success(stack, false);
        }

        ConstructEntity.Kind kind = getKind(stack);
        int cost = kind == ConstructEntity.Kind.FIST ? 120 : 60;
        if (!data.consumeCharge(cost)) {
            player.sendMessage(Text.literal("Кольцо разряжено! Заряди у Фонаря или Батареи.").formatted(Formatting.RED), true);
            return TypedActionResult.fail(stack);
        }

        ConstructEntity construct = ModEntities.CONSTRUCT.create(world);
        if (construct != null) {
            float mult = SpectrumSynergy.getPowerMultiplier(player);
            construct.configure(player, kind, corps, data.getDisplayArgb(), mult);
            world.spawnEntity(construct);
            world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, SoundCategory.PLAYERS, 0.8f, 1.25f);
            player.getItemCooldownManager().set(this, 8);
        }
        return TypedActionResult.success(stack, false);
    }

    private void activateNewRing(PlayerEntity player, PlayerLanternComponent data) {
        data.activateCorps(corps);
        data.setSuitActive(true);
        data.setAuraActive(true);
        data.setInsigniaActive(true);
        data.setGlowingEyesMask(DEFAULT_EYES_MASK);
        updateFlight(player, true);
        player.sendMessage(corps.getFormattedName().copy().append(" — кольцо надето!"), true);
    }

    private static void updateFlight(PlayerEntity player, boolean canFly) {
        if (player.isCreative() || player.isSpectator()) return;
        player.getAbilities().allowFlying = canFly;
        if (!canFly) player.getAbilities().flying = false;
        player.sendAbilitiesUpdate();
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient && entity instanceof PlayerEntity player) {
            handleRingTick(player, world);
        }
    }

    private void handleRingTick(PlayerEntity player, World world) {
        if (world.getTime() % 20 != 0) return;
        PlayerLanternComponent data = ModComponents.LANTERN.get(player);
        if (data.getActiveCorps() == corps && data.isSuitActive()) {
            if (player.getAbilities().flying && !player.isCreative()) {
                if (!data.consumeCharge(4)) updateFlight(player, false);
            } else if (data.getCharge() > 0 && !player.getAbilities().allowFlying) {
                updateFlight(player, true);
            }
        }
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(corps.getFormattedName());
        tooltip.add(Text.literal("Надень в слот кольца (Trinkets) или используй в руке").formatted(Formatting.GOLD));
        tooltip.add(Text.literal("Режим: " + getKind(stack).name() + " (Shift+ПКМ)").formatted(Formatting.AQUA));
    }
}
