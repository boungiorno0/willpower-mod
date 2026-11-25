package com.example.willpower.mixin;

import com.example.willpower.component.ModComponents;
import com.example.willpower.component.PlayerLanternComponent;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
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
        int pose = data.getActivePose();

        if (flying) {
            // Классический полёт Фонаря: ведущая рука с кольцом вытянута вперёд
            ringArm.pitch = -1.62f + (float) Math.toRadians(headPitch) * 0.45f;
            ringArm.yaw = 0.0f;
            ringArm.roll = 0.08f * sideSign;
            offArm.pitch = 0.35f;
            offArm.roll = -0.18f * sideSign;
            m.rightLeg.pitch = 0.28f;
            m.leftLeg.pitch = 0.42f;
        } else if (pose == 1) {
            // Боевая стойка: рука направлена вперёд
            ringArm.pitch = -1.45f;
            ringArm.yaw = 0.0f;
            offArm.pitch = 0.3f;
        } else if (pose == 2) {
            // Героическая поза: кулак вверх
            ringArm.pitch = -2.65f;
            ringArm.roll = 0.15f * sideSign;
            offArm.pitch = 0.25f;
        } else if (pose == 3) {
            // Поза медитации (для Синих/Белых и парения)
            m.rightArm.pitch = -0.85f;
            m.rightArm.yaw = -0.45f;
            m.leftArm.pitch = -0.85f;
            m.leftArm.yaw = 0.45f;
            m.rightLeg.pitch = -1.05f;
            m.rightLeg.roll = -0.35f;
            m.leftLeg.pitch = -1.05f;
            m.leftLeg.roll = 0.35f;
        }

        // Синхронизируем второй слой скина с руками и ногами
        m.rightSleeve.copyTransform(m.rightArm);
        m.leftSleeve.copyTransform(m.leftArm);
        m.rightPants.copyTransform(m.rightLeg);
        m.leftPants.copyTransform(m.leftLeg);
    }
}
