package com.example.willpower.mixin;

import com.example.willpower.client.FlightAnimState;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {

    @Inject(method = "setupTransforms(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/util/math/MatrixStack;FFFF)V", at = @At("TAIL"))
    private void willpower$tiltBodyInFlight(AbstractClientPlayerEntity player, MatrixStack matrices,
                                            float animationProgress, float bodyYaw, float tickDelta, float scale, CallbackInfo ci) {
        float tilt = FlightAnimState.getTilt(player);
        if (tilt < 0.01f || player.isFallFlying() || player.isSwimming()) return;

        float pitch = MathHelper.lerp(tickDelta, player.prevPitch, player.getPitch());
        float angleX = (-82.0f - MathHelper.clamp(pitch, -55f, 55f) * 0.65f) * tilt;
        float rollZ = FlightAnimState.getRoll(player);

        matrices.translate(0.0, 0.9 * tilt, 0.0);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(angleX));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rollZ));
        matrices.translate(0.0, -0.9 * tilt, 0.0);
    }
}
