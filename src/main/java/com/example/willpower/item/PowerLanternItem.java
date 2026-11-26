package com.example.willpower.item;

import com.example.willpower.ModBlocks;
import com.example.willpower.block.PowerLanternBlockEntity;
import com.example.willpower.client.render.PowerLanternGeoRenderers;
import com.example.willpower.component.ModComponents;
import com.example.willpower.component.PlayerLanternComponent;
import com.example.willpower.core.LanternCorps;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class PowerLanternItem extends BlockItem implements GeoItem {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.power_lantern.idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final LanternCorps corps;

    public PowerLanternItem(LanternCorps corps, Settings settings) {
        super(ModBlocks.POWER_LANTERN, settings);
        this.corps = corps;
    }

    public LanternCorps getCorps() { return corps; }

    @Override
    protected boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state) {
        if (world.getBlockEntity(pos) instanceof PowerLanternBlockEntity be) {
            be.setCorps(corps);
        }
        return super.postPlacement(pos, world, player, stack, state);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (world.isClient) return TypedActionResult.success(stack, true);

        PlayerLanternComponent data = ModComponents.LANTERN.get(player);
        if (data.getActiveCorps() != corps) {
            player.sendMessage(Text.literal("Этот фонарь настроен на ").formatted(Formatting.GRAY).append(corps.getFormattedName()), true);
            return TypedActionResult.fail(stack);
        }

        data.recharge(2500, true);
        player.sendMessage(corps.getOathText().copy().styled(s -> s.withColor(corps.getBaseRgb())), false);
        world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1.0f, 1.2f);

        if (world instanceof ServerWorld sw) {
            int rgb = corps.getBaseRgb();
            Vector3f color = new Vector3f(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f);
            sw.spawnParticles(new DustParticleEffect(color, 1.4f), player.getX(), player.getBodyY(0.5), player.getZ(), 24, 0.6, 0.7, 0.6, 0.04);
        }
        player.getItemCooldownManager().set(this, 40);
        return TypedActionResult.success(stack, false);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private PowerLanternGeoRenderers.ItemGeoRenderer renderer;

            @Override
            public BuiltinModelItemRenderer getGeoItemRenderer() {
                if (this.renderer == null) this.renderer = new PowerLanternGeoRenderers.ItemGeoRenderer();
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 0, state -> state.setAndContinue(IDLE_ANIM)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
