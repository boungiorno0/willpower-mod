package com.example.willpower.block;

import com.example.willpower.item.WillRingItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Хранилище воли: подзаряжает кольцо при ПКМ. */
public class WillReservoirBlock extends Block {
    public WillReservoirBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos,
                                             PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof WillRingItem) {
            if (!world.isClient) {
                if (stack.getDamage() == 0) {
                    player.sendMessage(Text.translatable("message.willpower.already_full"), true);
                } else {
                    stack.setDamage(0);
                    world.playSound(null, pos, SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.BLOCKS, 1.0f, 1.3f);
                    ((ServerWorld) world).spawnParticles(ParticleTypes.HAPPY_VILLAGER,
                            pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                            20, 0.4, 0.4, 0.4, 0.05);
                    player.sendMessage(Text.translatable("message.willpower.recharged"), true);
                }
            }
            return ItemActionResult.success(world.isClient);
        }
       return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
