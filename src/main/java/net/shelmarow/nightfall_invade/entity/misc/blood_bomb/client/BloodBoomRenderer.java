package net.shelmarow.nightfall_invade.entity.misc.blood_bomb.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shelmarow.nightfall_invade.entity.misc.blood_bomb.BloodBoom;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class BloodBoomRenderer extends EntityRenderer<BloodBoom> {

    public BloodBoomRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(@NotNull BloodBoom entity, float yaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int light) {
        super.render(entity, yaw, partialTick, poseStack, buffer, light);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull BloodBoom entity) {
        return ResourceLocation.parse("");
    }
}
