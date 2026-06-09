package net.shelmarow.nightfall_invade.entity.scarlet_hunter.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.shelmarow.nightfall_invade.NightFallInvade;
import net.shelmarow.nightfall_invade.entity.scarlet_hunter.ScarletHunter;
import org.jetbrains.annotations.NotNull;

public class BloodShieldLayer extends RenderLayer<ScarletHunter, HumanoidModel<ScarletHunter>> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "textures/entity/scarlet_hunter/blood_shield.png");

    public BloodShieldLayer(RenderLayerParent<ScarletHunter, HumanoidModel<ScarletHunter>> pRenderer) {
        super(pRenderer);
    }


    @Override
    public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int light, @NotNull ScarletHunter scarletHunter, float pLimbSwing, float pLimbSwingAmount, float partialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {

    }
}
