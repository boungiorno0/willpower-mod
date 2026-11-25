package com.example.willpower.item;

import com.example.willpower.component.ModComponents;
import com.example.willpower.component.PlayerLanternComponent;
import com.example.willpower.core.LanternCorps;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.joml.Vector3f;

public class PowerLanternItem extends Item {
    private final LanternCorps corps;

    public PowerLanternItem(LanternCorps corps, Settings settings) {
        super(settings);
        this.corps = corps;
    }

    public LanternCorps getCorps() {
        return corps;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (world.isClient) return TypedActionResult.success(stack, true);

        PlayerLanternComponent data = ModComponents.LANTERN.get(player);
        if (data.getActiveCorps() != corps) {
            player.sendMessage(Text.literal("Этот фонарь настроен на ").formatted(Formatting.GRAY)
                    .append(corps.getFormattedName()), true);
            return TypedActionResult.fail(stack);
        }

        data.recharge(2500, true);
        player.sendMessage(corps.getOathText().copy().styled(s -> s.withColor(corps.getBaseRgb())), false);
        world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_POWER_SELECT,
                SoundCategory.PLAYERS, 1.0f, 1.2f);

        if (world instanceof ServerWorld sw) {
            int rgb = corps.getBaseRgb();
            Vector3f color = new Vector3f(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f);
            sw.spawnParticles(new DustParticleEffect(color, 1.5f),
                    player.getX(), player.getBodyY(0.5), player.getZ(),
                    28, 0.7, 0.8, 0.7, 0.04);
        }

        player.getItemCooldownManager().set(this, 40);
        return TypedActionResult.success(stack, false);
    }
}
