package net.shelmarow.nightfall_invade.entity.misc.blood_slash;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shelmarow.nightfall_invade.NightFallInvade;
import net.shelmarow.nightfall_invade.entity.EmptyModel;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class BloodSlashRenderer extends MobRenderer<BloodSlashEntity, EmptyModel<BloodSlashEntity>> {

    public BloodSlashRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new EmptyModel<>(), 0F);
    }

    public boolean shouldRender(@NotNull BloodSlashEntity vacuumSliceEntity, @NotNull Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        return true;
    }

    @Override
    public void render(@NotNull BloodSlashEntity entity, float entityYaw, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Nullable
    protected RenderType getRenderType(@NotNull BloodSlashEntity vacuumSliceEntity, boolean pBodyVisible, boolean pTranslucent, boolean pGlowing) {
        return null;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull BloodSlashEntity pEntity) {
        return ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "textures/entity/blood_slash.png");
    }
}
