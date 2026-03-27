package net.shelmarow.nightfall_invade.entity.blood_hunter.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;
import net.shelmarow.nightfall_invade.entity.blood_hunter.ScarletHunter;
import net.shelmarow.nightfall_invade.entity.blood_hunter.ScarletHunterPatch;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.patched.entity.PHumanoidRenderer;

public class PScarletHunterRenderer extends PHumanoidRenderer<ScarletHunter, ScarletHunterPatch, HumanoidModel<ScarletHunter>, ScarletHunterRenderer, HumanoidMesh> {

    public PScarletHunterRenderer(AssetAccessor<HumanoidMesh> mesh, EntityRendererProvider.Context context, EntityType<?> entityType) {
        super(mesh, context, entityType);
        this.addPatchedLayer(BloodShieldLayer.class, new PatchedBloodShieldLayer());
    }
}
