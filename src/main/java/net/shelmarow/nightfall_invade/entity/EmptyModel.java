package net.shelmarow.nightfall_invade.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class EmptyModel<T extends LivingEntity> extends EntityModel<T>{

    @Override
    public void setupAnim(@NotNull T t, float v, float v1, float v2, float v3, float v4) {
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int i, int i1, float v, float v1, float v2, float v3) {
    }
}
