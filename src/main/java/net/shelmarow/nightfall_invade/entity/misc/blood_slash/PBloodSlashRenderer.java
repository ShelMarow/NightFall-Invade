package net.shelmarow.nightfall_invade.entity.misc.blood_slash;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.MinecraftForge;
import net.shelmarow.nightfall_invade.assets.NFIMeshes;
import net.shelmarow.nightfall_invade.entity.EmptyModel;
import org.joml.Vector4f;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.forgeevent.PrepareModelEvent;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.Vec2i;
import yesman.epicfight.client.renderer.patched.entity.PatchedLivingEntityRenderer;

public class PBloodSlashRenderer extends PatchedLivingEntityRenderer<BloodSlashEntity, BloodSlashPatch,
        EmptyModel<BloodSlashEntity>, BloodSlashRenderer, SkinnedMesh> {

    public PBloodSlashRenderer(EntityRendererProvider.Context context, EntityType<?> entityType) {
        super(context, entityType);
    }

    @Override
    public void render(BloodSlashEntity entity, BloodSlashPatch entitypatch, BloodSlashRenderer renderer, MultiBufferSource buffer, PoseStack poseStack, int packedLight, float partialTicks) {
        super.render(entity, entitypatch, renderer, buffer, poseStack, packedLight, partialTicks);

        poseStack.pushPose();

        RenderType renderType = RenderType.entityTranslucentEmissive(renderer.getTextureLocation(entity));
        Armature armature = entitypatch.getArmature();

        this.mulPoseStack(poseStack, armature, entity, entitypatch, partialTicks);
        this.prepareVanillaModel(entity, renderer.getModel(), renderer, partialTicks);
        this.setArmaturePose(entitypatch, armature, partialTicks);

        SkinnedMesh mesh = this.getMeshProvider(entitypatch).get();
        this.prepareModel(mesh, entity, entitypatch, renderer);

        PrepareModelEvent prepareModelEvent = new PrepareModelEvent(this, mesh, entitypatch, buffer, poseStack, packedLight, partialTicks);

        if (!MinecraftForge.EVENT_BUS.post(prepareModelEvent)) {
            float alpha = 1;

            AnimationPlayer animationPlayer = entitypatch.getAnimator().getPlayerFor(null);
            if(animationPlayer != null) {
                float progress = Mth.clamp(animationPlayer.getElapsedTime() / animationPlayer.getAnimation().get().getTotalTime(),0,1);
                float startFade = 0.7F;
                if(progress >= startFade){
                    alpha = 1 - (progress - startFade) / (1-startFade);
                }
            }

            Vector4f color = new Vector4f(1.0F, 1.0F, 1.0F, alpha);
            entitypatch.getEntityDecorations().modifyColor(color, partialTicks);

            int blockLight = 15;
            int skyLight = 15;
            Vec2i lightUv = new Vec2i(blockLight, skyLight);
            entitypatch.getEntityDecorations().modifyLight(lightUv, partialTicks);
            int modifiedLight = 255;
            mesh.draw(poseStack, buffer, renderType, modifiedLight, color.x(), color.y(), color.z(), color.w(), this.getOverlayCoord(entity, entitypatch, partialTicks), armature, armature.getPoseMatrices());

            entitypatch.getEntityDecorations().listDecorationOverlays().forEach(decorationOverlay -> {
                if (!decorationOverlay.shouldRemove() && decorationOverlay.shouldRender()) {
                    Vector4f overlayColor = decorationOverlay.color(partialTicks);
                    mesh.draw(poseStack, buffer, decorationOverlay.getRenderType(), modifiedLight, overlayColor.x(), overlayColor.y(), overlayColor.z(), overlayColor.w(), OverlayTexture.NO_OVERLAY, armature, armature.getPoseMatrices());
                }
            });
        }

        if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
            entitypatch.getClientAnimator().renderDebuggingInfoForAllLayers(poseStack, buffer, partialTicks);
        }

        poseStack.popPose();
    }


    @Override
    public AssetAccessor<SkinnedMesh> getDefaultMesh() {
        return NFIMeshes.BLOOD_SLASH_MESH;
    }
}
