package com.example.willpower.mixin;

import com.example.willpower.client.FlightAnimState;
import com.example.willpower.component.ModComponents;
import com.example.willpower.component.PlayerLanternComponent;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityModel.class)
public class PlayerEntityModelMixin<T extends LivingEntity> {

    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void willpower$applyLanternAnimations(T entity, float limbAngle, float limbDistance,
                                                  float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof AbstractClientPlayerEntity player)) return;
        PlayerLanternComponent data = ModComponents.LANTERN.get(player);
        if (data.getActiveCorps() == null || !data.isSuitActive()) return;

        PlayerEntityModel<?> m = (PlayerEntityModel<?>) (Object) this;
        boolean leftMain = player.getMainArm() == Arm.LEFT;
        ModelPart ringArm = leftMain ? m.leftArm : m.rightArm;
        ModelPart offArm = leftMain ? m.rightArm : m.leftArm;
        float sideSign = leftMain ? -1.0f : 1.0f;

        boolean flying = player.getAbilities().flying || player.isFallFlying();
        float tilt = FlightAnimState.getTilt(player);
        int pose = data.getActivePose();

        if (flying) {
            float sway = (float) Math.sin(animationProgress * 0.14f) * 0.06f;
            // Плавный переход от парения (рука вперёд) к полному полёту (рука вытянута над головой)
            ringArm.pitch = MathHelper.lerp(tilt, -1.45f, -3.08f);
            ringArm.yaw = 0.0f;
            ringArm.roll = MathHelper.lerp(tilt, 0.08f, -0.05f) * sideSign;

            offArm.pitch = MathHelper.lerp(tilt, 0.25f, 0.12f) + sway;
            offArm.roll = MathHelper.lerp(tilt, -0.18f, -0.10f) * sideSign;

            m.head.pitch = MathHelper.lerp(tilt, m.head.pitch, -1.05f);
            m.rightLeg.pitch = MathHelper.lerp(tilt, 0.22f, 0.06f) + sway;
            m.leftLeg.pitch = MathHelper.lerp(tilt, 0.34f, 0.12f) - sway;
        } else if (pose == 1) {
            ringArm.pitch = -1.45f;
            ringArm.yaw = 0.0f;
            offArm.pitch = 0.3f;
        } else if (pose == 2) {
            ringArm.pitch = -2.65f;
            ringArm.roll = 0.15f * sideSign;
            offArm.pitch = 0.25f;
        } else if (pose == 3) {
            m.rightArm.pitch = -0.85f;
            m.rightArm.yaw = -0.45f;
            m.leftArm.pitch = -0.85f;
            m.leftArm.yaw = 0.45f;
            m.rightLeg.pitch = -1.05f;
            m.rightLeg.roll = -0.35f;
            m.leftLeg.pitch = -1.05f;
            m.leftLeg.roll = 0.35f;
        }

        m.hat.copyTransform(m.head);
        m.rightSleeve.copyTransform(m.rightArm);
        m.leftSleeve.copyTransform(m.leftArm);
        m.rightPants.copyTransform(m.rightLeg);
        m.leftPants.copyTransform(m.leftLeg);
    }
}
