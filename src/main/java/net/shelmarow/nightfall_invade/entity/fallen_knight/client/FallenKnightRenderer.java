package net.shelmarow.nightfall_invade.entity.fallen_knight.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shelmarow.nightfall_invade.NightFallInvade;
import net.shelmarow.nightfall_invade.entity.blood_hunter.ScarletHunter;
import net.shelmarow.nightfall_invade.entity.blood_hunter.client.BloodShieldLayer;
import net.shelmarow.nightfall_invade.entity.fallen_knight.FallenKnight;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class FallenKnightRenderer extends HumanoidMobRenderer<FallenKnight, HumanoidModel<FallenKnight>> {

    public FallenKnightRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
    }

    @Override
    public boolean shouldRender(@NotNull FallenKnight pLivingEntity, @NotNull Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        return true;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull FallenKnight pEntity) {
        return ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "textures/entity/fallen_knight.png");
    }
}
