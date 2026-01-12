package net.shelmarow.nightfall_invade.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shelmarow.nightfall_invade.NightFallInvade;
import net.shelmarow.nightfall_invade.entity.NFIEntities;
import net.shelmarow.nightfall_invade.entity.spear_knight.ArteriusRenderer;
import yesman.epicfight.api.client.forgeevent.PatchedRenderersEvent;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.client.renderer.patched.entity.PHumanoidRenderer;

@Mod.EventBusSubscriber(modid = NightFallInvade.MOD_ID,bus = Mod.EventBusSubscriber.Bus.MOD,value = Dist.CLIENT)
public class ModEventClient {

    @SubscribeEvent
    public static void rendererRegister(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(NFIEntities.ARTERIUS.get(), ArteriusRenderer::new);
    }

    @SubscribeEvent
    public static void onPatchedRenderer(PatchedRenderersEvent.Add event){
        event.addPatchedEntityRenderer(NFIEntities.ARTERIUS.get(),
                entityType -> new PHumanoidRenderer<>(Meshes.BIPED_OLD_TEX,event.getContext(), entityType)
                        .initLayerLast(event.getContext(), entityType));
    }
}
