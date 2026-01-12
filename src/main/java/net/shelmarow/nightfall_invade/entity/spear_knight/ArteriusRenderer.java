package net.shelmarow.nightfall_invade.entity.spear_knight;

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
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class ArteriusRenderer extends HumanoidMobRenderer<Arterius, HumanoidModel<Arterius>> {

    public ArteriusRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
    }


    @Override
    public boolean shouldRender(@NotNull Arterius pLivingEntity, @NotNull Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        return true;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Arterius pEntity) {
        return ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "textures/entity/knight.png");
    }
}
