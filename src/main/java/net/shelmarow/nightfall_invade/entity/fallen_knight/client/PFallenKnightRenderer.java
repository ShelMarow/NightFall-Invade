package net.shelmarow.nightfall_invade.entity.fallen_knight.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;
import net.shelmarow.nightfall_invade.entity.blood_hunter.ScarletHunter;
import net.shelmarow.nightfall_invade.entity.blood_hunter.ScarletHunterPatch;
import net.shelmarow.nightfall_invade.entity.blood_hunter.client.BloodShieldLayer;
import net.shelmarow.nightfall_invade.entity.blood_hunter.client.PatchedBloodShieldLayer;
import net.shelmarow.nightfall_invade.entity.blood_hunter.client.ScarletHunterRenderer;
import net.shelmarow.nightfall_invade.entity.fallen_knight.FallenKnight;
import net.shelmarow.nightfall_invade.entity.fallen_knight.FallenKnightPatch;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.patched.entity.PHumanoidRenderer;

public class PFallenKnightRenderer extends PHumanoidRenderer<FallenKnight, FallenKnightPatch, HumanoidModel<FallenKnight>, FallenKnightRenderer, HumanoidMesh> {

    public PFallenKnightRenderer(AssetAccessor<HumanoidMesh> mesh, EntityRendererProvider.Context context, EntityType<?> entityType) {
        super(mesh, context, entityType);
    }
}
