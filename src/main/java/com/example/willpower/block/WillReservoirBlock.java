package com.example.willpower.block;

import com.example.willpower.component.ModComponents;
import com.example.willpower.component.PlayerLanternComponent;
import com.example.willpower.core.LanternCorps;
import com.example.willpower.world.CentralBatteryMultiblock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.joml.Vector3f;

public class WillReservoirBlock extends Block {
    public WillReservoirBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;

        PlayerLanternComponent data = ModComponents.LANTERN.get(player);
        LanternCorps completed = CentralBatteryMultiblock.detectCompleted(world, pos);

        if (completed != null) {
            if (data.getActiveCorps() == null || data.getActiveCorps() != completed) {
                data.activateCorps(completed);
                data.setSuitActive(true);
            } else {
                data.recharge(data.getMaxCharge(), true);
            }
            spawnBeamParticles((ServerWorld) world, pos, completed.getBaseRgb(), 45);
            world.playSound(null, pos, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1.2f, 1.0f);
            player.sendMessage(Text.literal("Центральная Батарея синхронизирована: ")
                    .append(completed.getFormattedName()), true);
            return ActionResult.CONSUME;
        }

        if (data.getActiveCorps() == null) {
            player.sendMessage(Text.literal("Построй вокруг блока башню из 66 блоков одного из корпусов!")
                    .formatted(Formatting.YELLOW), true);
            return ActionResult.PASS;
        }

        int matched = CentralBatteryMultiblock.countMatching(world, pos, data.getActiveCorps());
        int total = CentralBatteryMultiblock.totalRequiredSurrounding();
        data.recharge(data.getMaxCharge() / 2, false);
        spawnBeamParticles((ServerWorld) world, pos, data.getActiveCorps().getBaseRgb(), 15);
        world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.BLOCKS, 0.9f, 1.2f);
        player.sendMessage(Text.literal("Мини-заряд (+50%). Батарея собрана: " + matched + "/" + total)
                .formatted(Formatting.AQUA), true);
        return ActionResult.CONSUME;
    }

    private static void spawnBeamParticles(ServerWorld sw, BlockPos pos, int rgb, int count) {
        Vector3f col = new Vector3f(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f);
        sw.spawnParticles(new DustParticleEffect(col, 1.8f),
                pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                count, 0.6, 2.4, 0.6, 0.03);
    }
}
