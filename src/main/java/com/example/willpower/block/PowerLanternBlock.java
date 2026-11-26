package com.example.willpower.block;

import com.example.willpower.ModCorpsRegistry;
import com.example.willpower.component.ModComponents;
import com.example.willpower.component.PlayerLanternComponent;
import com.example.willpower.core.LanternCorps;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.joml.Vector3f;

public class PowerLanternBlock extends BlockWithEntity {
    public static final MapCodec<PowerLanternBlock> CODEC = createCodec(PowerLanternBlock::new);
    private static final VoxelShape SHAPE = Block.createCuboidShape(3, 0, 3, 13, 15, 13);

    public PowerLanternBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() { return CODEC; }

    @Override
    public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.ENTITYBLOCK_ANIMATED; }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext ctx) { return SHAPE; }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PowerLanternBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;
        if (!(world.getBlockEntity(pos) instanceof PowerLanternBlockEntity be)) return ActionResult.PASS;

        LanternCorps corps = be.getCorps();
        if (player.isSneaking()) {
            player.getInventory().offerOrDrop(new ItemStack(ModCorpsRegistry.LANTERNS.get(corps)));
            world.removeBlock(pos, false);
            world.playSound(null, pos, SoundEvents.BLOCK_METAL_BREAK, SoundCategory.BLOCKS, 0.8f, 1.2f);
            return ActionResult.CONSUME;
        }

        PlayerLanternComponent data = ModComponents.LANTERN.get(player);
        if (data.getActiveCorps() != corps) {
            player.sendMessage(Text.literal("Этот фонарь питает ").formatted(Formatting.GRAY).append(corps.getFormattedName()), true);
            return ActionResult.FAIL;
        }

        data.recharge(2500, true);
        player.sendMessage(corps.getOathText().copy().styled(s -> s.withColor(corps.getBaseRgb())), false);
        world.playSound(null, pos, SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.BLOCKS, 1.0f, 1.3f);

        if (world instanceof ServerWorld sw) {
            int rgb = corps.getBaseRgb();
            Vector3f col = new Vector3f(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f);
            DustParticleEffect dust = new DustParticleEffect(col, 1.2f);
            Vec3d from = Vec3d.ofCenter(pos);
            Vec3d to = player.getEyePos().add(0, -0.25, 0);
            for (int i = 0; i <= 12; i++) {
                Vec3d p = from.lerp(to, i / 12.0);
                sw.spawnParticles(dust, p.x, p.y, p.z, 2, 0.04, 0.04, 0.04, 0);
            }
        }
        return ActionResult.CONSUME;
    }
}
