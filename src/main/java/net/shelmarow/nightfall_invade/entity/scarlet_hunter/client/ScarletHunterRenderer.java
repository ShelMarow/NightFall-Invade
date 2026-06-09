package net.shelmarow.nightfall_invade.entity.scarlet_hunter.client;

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
import net.shelmarow.nightfall_invade.entity.scarlet_hunter.ScarletHunter;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class ScarletHunterRenderer extends HumanoidMobRenderer<ScarletHunter, HumanoidModel<ScarletHunter>> {

    public ScarletHunterRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
        this.addLayer(new BloodShieldLayer(this));
    }

    @Override
    public boolean shouldRender(@NotNull ScarletHunter pLivingEntity, @NotNull Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        return true;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull ScarletHunter pEntity) {
        return ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "textures/entity/scarlet_hunter/scarlet_hunter.png");
    }
}
