package net.shelmarow.nightfall_invade.entity.scarlet_hunter.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.shelmarow.nightfall_invade.NightFallInvade;
import net.shelmarow.nightfall_invade.entity.scarlet_hunter.ScarletHunter;
import net.shelmarow.nightfall_invade.entity.scarlet_hunter.ScarletHunterPatch;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.renderer.patched.layer.ModelRenderLayer;

public class PatchedBloodShieldLayer extends ModelRenderLayer<ScarletHunter, ScarletHunterPatch, HumanoidModel<ScarletHunter>, BloodShieldLayer, HumanoidMesh> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "textures/entity/scarlet_hunter/blood_shield.png");

    public PatchedBloodShieldLayer() {
        super(Meshes.BIPED_OLD_TEX);
    }

    @Override
    protected void renderLayer(ScarletHunterPatch entitypatch, ScarletHunter scarletHunter, @Nullable BloodShieldLayer vanillaLayer, PoseStack poseStack, MultiBufferSource buffer, int packedLight, OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks) {
        if(entitypatch.getOriginal().hasBloodShield()){
            poseStack.pushPose();

            float time = scarletHunter.tickCount + partialTicks;

            RenderType renderType = EpicFightRenderTypes.makeTriangulated(RenderType.energySwirl(TEXTURE, time * 0.015F, time * 0.015F));
            VertexConsumer consumer = buffer.getBuffer(renderType);
            this.mesh.get().drawPosed(
                    poseStack, consumer, Mesh.DrawingFunction.NEW_ENTITY,
                    255, 1F, 1F, 1F, 1F,
                    OverlayTexture.NO_OVERLAY, entitypatch.getArmature(), poses
            );

            poseStack.popPose();
        }
    }
}
