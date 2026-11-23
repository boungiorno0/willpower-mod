package com.example.willpower.item;

import com.example.willpower.FlightHandler;
import com.example.willpower.ModArmorMaterials;
import com.example.willpower.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class WillRingItem extends Item {
    private static final String MODE_KEY = "willpower_mode";

    public WillRingItem(Settings settings) {
        super(settings);
    }

    // ---------- режим ----------

    public static RingMode getMode(ItemStack stack) {
        NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        return RingMode.byId(nbt.getInt(MODE_KEY));
    }

    private static void setMode(ItemStack stack, RingMode mode) {
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> nbt.putInt(MODE_KEY, mode.ordinal()));
    }

    // ---------- использование ----------

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        // Shift + ПКМ: сменить режим
        if (player.isSneaking()) {
            if (!world.isClient) {
                RingMode next = getMode(stack).next();
                setMode(stack, next);
                player.sendMessage(Text.translatable("message.willpower.mode",
                        Text.translatable(next.translationKey())), true);
                world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
                        SoundCategory.PLAYERS, 1.0f, 1.2f);
            }
            return TypedActionResult.success(stack, world.isClient);
        }

        RingMode mode = getMode(stack);

        // Полёт: переключатель, заряд тратится не сразу, а пока игрок летит
        if (mode == RingMode.FLIGHT) {
            if (!world.isClient) {
                FlightHandler.toggle((ServerPlayerEntity) player, stack);
            }
            return TypedActionResult.success(stack, world.isClient);
        }

        // Не хватает заряда (кольцо никогда не ломается)
        if (stack.getDamage() + mode.cost >= stack.getMaxDamage()) {
            if (!world.isClient) {
                player.sendMessage(Text.translatable("message.willpower.no_charge"), true);
            }
            return TypedActionResult.fail(stack);
        }

        if (!world.isClient) {
            ServerWorld serverWorld = (ServerWorld) world;
            switch (mode) {
                case BRIDGE -> buildBridge(serverWorld, player);
                case WALL -> buildWall(serverWorld, player);
                case PLATFORM -> buildPlatform(serverWorld, player);
                case SHIELD -> buildShield(serverWorld, player);
                case DASH -> dash(player);
                case FLIGHT -> { }
            }
            stack.setDamage(stack.getDamage() + mode.cost);
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.PLAYERS, 1.0f, 0.9f);
            serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER,
                    player.getX(), player.getBodyY(0.5), player.getZ(),
                    12, 0.4, 0.4, 0.4, 0.02);
            player.getItemCooldownManager().set(this, mode == RingMode.DASH ? 8 : 5);
        }
        return TypedActionResult.success(stack, world.isClient);
    }

    // ---------- конструкты ----------

    private static void place(World world, BlockPos pos) {
        BlockState existing = world.getBlockState(pos);
        if (existing.isAir() || existing.isReplaceable()) {
            world.setBlockState(pos, ModBlocks.WILL_CONSTRUCT.getDefaultState());
        }
    }

    /** Мост шириной 3, длиной 16 вдоль взгляда, чуть ниже ног. */
    private static void buildBridge(World world, PlayerEntity player) {
        Vec3d look = player.getRotationVec(1.0f);
        Vec3d right = new Vec3d(-look.z, 0, look.x);
        if (right.lengthSquared() > 1.0E-6) {
            right = right.normalize();
        }
        Vec3d origin = player.getPos().add(0, -0.5, 0);
        for (int i = 1; i <= 16; i++) {
            Vec3d p = origin.add(look.multiply(i));
            for (int w = -1; w <= 1; w++) {
                place(world, BlockPos.ofFloored(p.add(right.multiply(w))));
            }
        }
    }

    /** Стена 5 x 4 в трёх блоках перед игроком. */
    private static void buildWall(World world, PlayerEntity player) {
        Direction facing = player.getHorizontalFacing();
        Direction side = facing.rotateYClockwise();
        BlockPos center = player.getBlockPos().offset(facing, 3);
        for (int w = -2; w <= 2; w++) {
            for (int h = 0; h < 4; h++) {
                place(world, center.offset(side, w).up(h));
            }
        }
    }

    /** Платформа 5 x 5 под ногами. */
    private static void buildPlatform(World world, PlayerEntity player) {
        BlockPos base = player.getBlockPos().down();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                place(world, base.add(x, 0, z));
            }
        }
    }

    /** Полая сфера радиусом 3 вокруг игрока. */
    private static void buildShield(World world, PlayerEntity player) {
        BlockPos c = player.getBlockPos();
        int r = 3;
        for (int dx = -r - 1; dx <= r + 1; dx++) {
            for (int dy = -r - 1; dy <= r + 1; dy++) {
                for (int dz = -r - 1; dz <= r + 1; dz++) {
                    double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (d >= r - 0.5 && d <= r + 0.5) {
                        place(world, c.add(dx, dy, dz));
                    }
                }
            }
        }
    }

    /** Рывок вперёд по направлению взгляда. */
    private static void dash(PlayerEntity player) {
        Vec3d look = player.getRotationVec(1.0f);
        player.setVelocity(look.multiply(1.8));
        player.velocityModified = true;
        player.fallDistance = 0.0f;
    }

    // ---------- прочее ----------

    /** Медленная саморегенерация: 1 единица заряда в 2 секунды (в полном костюме вдвое быстрее). */
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        int interval = (entity instanceof PlayerEntity p && ModArmorMaterials.hasFullSet(p)) ? 20 : 40;
        if (!world.isClient && world.getTime() % interval == 0 && stack.getDamage() > 0) {
            stack.setDamage(stack.getDamage() - 1);
        }
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        int charge = 100 - (stack.getDamage() * 100 / stack.getMaxDamage());
        tooltip.add(Text.translatable("tooltip.willpower.mode",
                Text.translatable(getMode(stack).translationKey())).formatted(Formatting.GREEN));
        tooltip.add(Text.translatable("tooltip.willpower.charge", charge).formatted(Formatting.DARK_GREEN));
        tooltip.add(Text.translatable("tooltip.willpower.hint").formatted(Formatting.GRAY));
    }
}
