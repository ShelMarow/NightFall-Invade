package net.shelmarow.nightfall_invade.event.mod;

import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shelmarow.nightfall_invade.NightFallInvade;
import net.shelmarow.nightfall_invade.entity.NFIEntities;
import net.shelmarow.nightfall_invade.entity.blood_hunter.client.PScarletHunterRenderer;
import net.shelmarow.nightfall_invade.entity.blood_hunter.client.ScarletHunterRenderer;
import net.shelmarow.nightfall_invade.entity.fallen_knight.client.FallenKnightRenderer;
import net.shelmarow.nightfall_invade.entity.fallen_knight.client.PFallenKnightRenderer;
import net.shelmarow.nightfall_invade.entity.misc.blood_bomb.client.BloodBoomRenderer;
import net.shelmarow.nightfall_invade.entity.misc.blood_slash.BloodSlashRenderer;
import net.shelmarow.nightfall_invade.entity.misc.blood_slash.PBloodSlashRenderer;
import net.shelmarow.nightfall_invade.entity.arterius.client.ArteriusRenderer;
import net.shelmarow.nightfall_invade.particle.NFIParticles;
import net.shelmarow.nightfall_invade.particle.particles.BloodParticleA;
import net.shelmarow.nightfall_invade.particle.particles.BloodParticleB;
import yesman.epicfight.api.client.forgeevent.PatchedRenderersEvent;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.client.renderer.patched.entity.PHumanoidRenderer;

@Mod.EventBusSubscriber(modid = NightFallInvade.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEventClient {


    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(NFIParticles.BLOOD_A.get(), BloodParticleA.Provider::new);
        event.registerSpriteSet(NFIParticles.BLOOD_B.get(), BloodParticleB.Provider::new);
    }

    @SubscribeEvent
    public static void rendererRegister(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(NFIEntities.ARTERIUS.get(), ArteriusRenderer::new);
        event.registerEntityRenderer(NFIEntities.SCARLET_HUNTER.get(), ScarletHunterRenderer::new);
        event.registerEntityRenderer(NFIEntities.FALLEN_KNIGHT.get(), FallenKnightRenderer::new);
        event.registerEntityRenderer(NFIEntities.BLOOD_BOOM.get(), BloodBoomRenderer::new);
        event.registerEntityRenderer(NFIEntities.BLOOD_SLASH.get(), BloodSlashRenderer::new);
    }

    @SubscribeEvent
    public static void onPatchedRenderer(PatchedRenderersEvent.Add event) {
        event.addPatchedEntityRenderer(NFIEntities.ARTERIUS.get(),
                entityType -> new PHumanoidRenderer<>(Meshes.BIPED_OLD_TEX, event.getContext(), entityType)
                        .initLayerLast(event.getContext(), entityType));

        event.addPatchedEntityRenderer(NFIEntities.SCARLET_HUNTER.get(),
                entityType -> new PScarletHunterRenderer(Meshes.BIPED_OLD_TEX, event.getContext(), entityType)
                        .initLayerLast(event.getContext(), entityType));

        event.addPatchedEntityRenderer(NFIEntities.FALLEN_KNIGHT.get(),
                entityType -> new PFallenKnightRenderer(Meshes.BIPED_OLD_TEX, event.getContext(), entityType)
                        .initLayerLast(event.getContext(), entityType));

        event.addPatchedEntityRenderer(NFIEntities.BLOOD_SLASH.get(),
                entityType -> new PBloodSlashRenderer(event.getContext(), entityType)
                        .initLayerLast(event.getContext(), entityType));
    }
}
